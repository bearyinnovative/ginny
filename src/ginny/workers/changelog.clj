(ns ginny.workers.changelog
  (:require [ginny.incoming :as incoming]
            [ginny.storages.github :as github]
            [ginny.storages.qiniu :as qiniu]
            [ginny.changelog :as cl]
            [ginny.helper :as helper]
            [ginny.config :as config]))

(defn fetch-by-platform
  [{:keys [repo path access-token private]}]
  (let [file-info (if private
                    (github/get-private-file-info repo path access-token)
                    (github/get-public-file-info repo path))
        file-url (:download_url file-info)
        changelog-md (if private
                       (github/read-private-file file-url
                                                 access-token)
                       (github/read-public-file file-url))]
    (if (some? changelog-md)
      changelog-md
      (throw (Exception. (str "cannot fetch changelog from github: "
                              (helper/map->json file-info)))))))

(defn json-file-name
  [key]
  (format "releases/%s-changelog.json" (name key)))

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
      resp
      (throw (Exception. (str "cannot upload changelog to qiniu: "
                              (helper/map->json resp)))))))

(defn md->changelog
  [md]
  (let [changelog (cl/parse-changelog md)]
    (if (cl/changelog-valid? changelog)
      changelog
      (throw (Exception. (str "cannot parse changelog: "
                              md))))))

(defn work
  [platform]
  (try
    (do
      (-> (fetch-by-platform platform)
          md->changelog
          upload)
      (incoming/report-success-message :changelog
                                       (str (name platform)
                                            " works fine")))
    (catch Exception e
      (incoming/report-error-message :changelog (.getMessage e)))))

(defn worker
  []
  (doall (map work (config/get-changelog-platforms))))
