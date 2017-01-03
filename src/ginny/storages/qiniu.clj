(ns ginny.storages.qiniu
  (:require [ginny.config :as config]
            [clj.qiniu :as qiniu]))

(defn init-config
  [access-key secret-key]
  (qiniu/set-config! :access-key access-key
                     :secret-key secret-key))

(defn upload-file
  "file should be clojure.java.io/input-stream"
  [file key bucket]
  (let [uptoken (qiniu/uptoken bucket
                              :scope (str bucket ":" key))]
    (qiniu/upload uptoken
                  key
                  file)))
