(ns ginny.workers.changelog
  (:require [clojure.string :as string]
            [clojure.data.json :as json])
  (:require [ginny.incoming :as incoming]
            [ginny.storages.github :as github]
            [ginny.storages.qiniu :as qiniu]
            [ginny.storages.rsync :as rsync]
            [ginny.changelog :as cl]
            [ginny.helper :as helper]
            [ginny.config :as config]))

(defn fetch-by-platform
  [{:keys [repo path access-token branch private]}]
  (let [file-info (if private
                    (github/get-private-file-info repo path branch access-token)
                    (github/get-public-file-info repo path branch))
        file-url (:download_url file-info)
        changelog-md (if private
                       (github/read-private-file file-url
                                                 access-token)
                       (github/read-public-file file-url))]
    (if (some? changelog-md)
      changelog-md
      (throw (Exception. (str "cannot fetch changelog from github: "
                              (helper/map->json file-info)))))))

; TODO move qiniu related process logic out
(defn json-file-name
  [key]
  (clojure.string/lower-case
    (format "releases/%s/changelog.json" (name key))))

(defn upload
  [changelog]
  (let [platform (cl/get-platform changelog)
        key (json-file-name platform)
        stream (-> changelog
                   helper/map->json
                   helper/string->stream)
        resp (qiniu/upload-file stream
                                key
                                (:bucket config/qiniu))]
    (if (:ok resp)
      key
      (throw (Exception. (str "cannot upload changelog to qiniu: "
                              (helper/map->json resp)))))))

(defn md->changelog
  [md]
  (let [changelog (cl/parse-changelog md)]
    (if (cl/changelog-valid? changelog)
      changelog
      (throw (Exception. (str "cannot parse changelog: "
                              md))))))

(defn- upload-to-qiniu
  [changelog-name changelog]
  (let [file-key (upload changelog)
        file-url (format "%s/%s" (:base-url config/qiniu) file-key)
        access-url (format "%s/%s" (:pretty-base-url config/qiniu) file-key)]
    (qiniu/refresh-cache :urls [file-url] :dirs [])
    (incoming/report-success-message
      :changelog-qiniu
      (format "changelog %s uploaded to qiniu\n %s"
              changelog-name access-url))))

(defn upload-to-qiniu!
  [& args]
  (try
    (apply upload-to-qiniu args)
    (catch Exception e
      (incoming/report-error-message :changelog-qiniu (.getMessage e)))))

(defn- upload-with-rsync
  [path changelog-name changelog]
  (let [[host _] (string/split path #":")
        remote-file-path
        (clojure.string/lower-case
          ; FIXME: join path with path library
          (format "%s%s.json" path (cl/get-platform changelog)))
        file-content (helper/map->json changelog)]
    (when-not (rsync/upload-file file-content remote-file-path)
      (throw (Exception. (format "upload to %s failed" remote-file-path))))
    (incoming/report-success-message
      :changelog-rsync
      (format "changelog %s uploaded to remote server: %s"
              changelog-name host))))

(defn upload-with-rsync!
  [paths & args]
  {:pre (seq? paths)}
  (doseq [p paths]
    (try
      (apply upload-with-rsync (cons p args))
      (catch Exception e
        (incoming/report-error-message :changelog-rsync (.getMessage e))))))

(defn generate-changelog!
  [platform]
  (let [changelog-name (name (:name platform))
        changelog (-> platform fetch-by-platform md->changelog)
        paths (:path-prefixs config/rsync)]
    (upload-to-qiniu! changelog-name changelog)
    (upload-with-rsync! paths changelog-name changelog)))

(defn build-and-upload
  []
  ; TODO: run in parallel
  (run! generate-changelog! (config/get-changelog-platforms)))

(defn build-changelog
  [platform {:keys [prefix] :as kwargs}]
  (let [prefix (or prefix "./changelog-")
        changelog-name (name (:name platform))
        target (format "%s%s.json" prefix changelog-name)
        changelog (-> platform fetch-by-platform md->changelog)
        content (json/write-str changelog)]
    (with-open [outfile (clojure.java.io/writer target)]
      (.write outfile content)
      (println (str "Wrote file: " target)))))

(defn build-all-changelogs
  [kwargs]
  (run! #(build-changelog % kwargs) (config/get-changelog-platforms)))
