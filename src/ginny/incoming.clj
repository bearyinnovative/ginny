(ns ginny.incoming
  (:require [ginny.helper :as helper]
            [ginny.config :as config]
            [clj-http.client :as http]))

(defn push-message
  [text & attachments]
  (let [url config/incoming-hook-url
        msg {:text text
             :markdown true
             :attachments attachments}]
    (http/post url {:form-params msg
                    :content-type :json})))

(defn create-error-attachment
  [title text]
  {:title title
   :text text
   :color "red"})

(defn create-success-attachment
  [title text]
  {:title title
   :text text
   :color "green"})

(defn report-error-message
  [worker content]
  (push-message (str (name worker) " failed")
                (create-error-attachment worker content)))

(defn report-success-message
  [worker content]
  (push-message (str (name worker) " success")
                (create-success-attachment worker content)))
