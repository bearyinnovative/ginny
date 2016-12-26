(ns ginny.worker
  (:require [ginny.config :as config]
            [ginny.storage :as storage]
            [ginny.changelog :as cl]
            [ginny.helper :as helper]
            [taoensso.timbre :as timbre]))

(defn get-changelog
[platform-key]
(let [{:keys [repo changelog-path access-token]} (platform-key config/platforms)
      file-info (storage/get-github-file-info repo changelog-path access-token)
      file-url (:download_url file-info)
      changelog-md (storage/read-github-file file-url access-token)]
  (cl/parse-changelog changelog-md)))

(defn gen-changelog-json-name
  [platform]
  (format "%s-changelog.json" (name platform)))

(defn upload-changelog
  [changelog]
  (let [platform (cl/get-platform changelog)
        key (gen-changelog-json-name platform)
        stream (-> changelog
                   helper/map->json
                   helper/string->stream)
        resp (storage/upload-file-to-qiniu stream key)]
    (when-not (:ok resp)
      (throw (Exception. (helper/map->json resp))))))

(defn changelog-worker
  []
  (let [platforms [:windows
                   :mac
                   :android
                   :ios
                   :linux]]
    (->> platforms
         (map get-changelog)
         (map upload-changelog)
         doall)))
