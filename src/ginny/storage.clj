(ns ginny.storage
  (:require [ginny.config :as config]
            [ginny.helper :as helper]
            [clj-http.client :as http]
            [clj.qiniu :as qiniu]))

(defn- get-github-request-headers
  [token]
  {:authorization (str "token " token)
   :content-type "application/vnd.github.v3.raw"})

(defn get-github-file-info
  [repo file-path token]
  (let [url (format "https://api.github.com/repos/%s/contents/%s"
                    repo file-path)
        headers (get-github-request-headers token)
        resp (http/get url headers)]
    (when (= (:status resp) 200)
      (helper/json->map (:body resp)))))

(defn read-github-file
  [url token]
  (let [headers (get-github-request-headers token)
        resp (http/get url headers)]
    (when (= (:status resp) 200)
      (:body resp))))

(defn upload-file-to-qiniu
  "file should be clojure.java.io/input-stream"
  [file key]
  (let [bucket (:bucket config/qiniu)
        uptoken (qiniu/uptoken bucket
                               :scope (str bucket ":" key))]
    (qiniu/upload uptoken
                  key
                  file)))
