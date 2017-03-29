(ns ginny.core
  (:require [ginny.config :as config]
            [ginny.workers.changelog :as changelog]
            [ginny.storages.qiniu :as qiniu])
  (:gen-class))

(defn- run-workers
  [workers]
  ; TODO: run in parallel
  (run! #(%) workers))

(defn -main
  "I don't do a whole lot."
  [& args]
  (let [{:keys [access-key secret-key]} config/qiniu
        workers [changelog/worker]]
    (qiniu/init-config access-key secret-key)
    (run-workers workers)))
