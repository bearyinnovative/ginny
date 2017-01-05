(ns ginny.storages.github
  (:require [ginny.helper :as helper]
            [clj-http.client :as http]))

(def ^:const RAW-CONTENT-TYPE "application/vnd.github.v3.raw")

(defn get-file-info
  [repo path branch headers]
  (let [url (format "https://api.github.com/repos/%s/contents/%s?ref=%s"
                    repo path branch)
        resp (http/get url headers)]
    (when (= (:status resp) 200)
      (helper/json->map (:body resp)))))

(defn get-public-file-info
  [repo path branch]
  (get-file-info repo
                 path
                 branch
                 {:content-type RAW-CONTENT-TYPE
                  :throw-exceptions false}))

(defn get-private-file-info
  [repo path branch token]
  (get-file-info repo
                 path
                 branch
                 {:authorization (str "token " token)
                  :content-type RAW-CONTENT-TYPE
                  :throw-exceptions false}))

(defn read-file
  [url headers]
  (let [resp (http/get url headers)]
    (when (= (:status resp) 200)
      (:body resp))))

(defn read-private-file
  [url token]
  (read-file url {:authorization (str "token " token)
                  :content-type RAW-CONTENT-TYPE
                  :throw-exceptions false}))

(defn read-public-file
  [url]
  (read-file url {:content-type RAW-CONTENT-TYPE
                  :throw-exceptions false}))

