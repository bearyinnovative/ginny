(ns ginny.helper
  (:require [clojure.string :as string]
            [cheshire.core :as json]))

(defn safe-empty?
  "data like int, keyword, double are considered not empty"
  [x]
  (try
    (empty? x)
    (catch IllegalArgumentException _
      false)))

(defn nil-or-empty?
  [v]
  ((some-fn nil? safe-empty?) v))

(defn split
  ([s re] (split s re -1))
  ([^String s re limit]
   (->> (string/split s re limit)
        (remove string/blank?)
        (map string/trim))))

(defn split-lines
  "split string"
  ([s] (split-lines s -1))
  ([^String s limit]
    (split s #"\n+" limit)))

(defn ->keyword
  [s]
  (if (string? s)
    (keyword (string/lower-case s))
    (keyword (name s))))

(defn map->json
  [m]
  (when (map? m)
    (json/generate-string m)))

(defn json->map
  [^String j]
  (json/parse-string j true))

(defn string->stream
  [^String s]
  (-> s
      (.getBytes "UTF-8")
      (java.io.ByteArrayInputStream.)))
