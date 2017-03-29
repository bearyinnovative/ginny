(ns ginny.workers.changelog
  (:require [ginny.incoming :as incoming]
            [ginny.storages.github :as github]
            [ginny.storages.qiniu :as qiniu]
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

(defn generate-changelog!
  [platform]
  (let [changelog-name (name (:name platform))
        changelog (-> platform fetch-by-platform md->changelog)]
    (upload-to-qiniu! changelog-name changelog)))

(defn worker
  []
  ; TODO: run in parallel
  (run! generate-changelog! (config/get-changelog-platforms)))
