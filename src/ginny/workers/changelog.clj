(ns ginny.workers.changelog
  (:require [ginny.incoming :as incoming]
            [ginny.storage :as storage]
            [ginny.changelog :as cl]
            [ginny.helper :as helper]
            [ginny.config :as config]))

(defn fetch-by-key
  [key]
  (let [{:keys [repo changelog-path access-token]} (key config/platforms)
        file-info (storage/get-github-file-info repo changelog-path access-token)
        file-url (:download_url file-info)
        changelog-md (storage/read-github-file file-url access-token)]
    (if-not (nil? changelog-md)
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
        resp (storage/upload-file-to-qiniu stream key)]
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
  (-> (fetch-by-key platform)
      md->changelog
      upload)
  (try
    (do
      (-> (fetch-by-key platform)
          md->changelog
          upload)
      (incoming/report-success-message :changelog
                                       (str (name platform)
                                            " works fine")))
    (catch Exception e
      (incoming/report-error-message :changelog (.getMessage e)))))

(defn worker
  []
  (let [platforms config/enabled-platforms]
    (doall (map work platforms))))
