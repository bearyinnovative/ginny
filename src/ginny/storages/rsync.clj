(ns ginny.storages.rsync
  (:require [clojure.java.io
             :refer [writer]]
            [clojure.java.shell
             :refer [sh]])
  (:import [java.io File]))

(defn- create-local-file!
  [file-content]
  (let [file (File/createTempFile "ginny" ".tmp")]
    (-> (writer file) (spit file-content))
    file))

(defn- upload
  [local-path remote-path]
  (sh "rsync" "-P" "--delete" local-path remote-path))

(defn upload-file
  [file-content remote-path]
  (let [local-file (create-local-file! file-content)]
    (upload (.getAbsolutePath local-file) remote-path)
    (.delete local-file)))
