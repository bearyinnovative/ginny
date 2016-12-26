(ns ginny.core
  (:require [ginny.config :as config]
            [ginny.worker :as worker]
            [taoensso.timbre :as timbre]
            [clj.qiniu :as qiniu]))

(defn work-forever
  [workers interval-millsecond]
  (loop [interval interval-millsecond]
    (try
      (mapv #(apply % nil) workers)
      (catch Exception e
        (timbre/error (.getMessage e))))
    (Thread/sleep interval)
    (recur interval)))

(defn -main
  "I don't do a whole lot."
  [& args]
  (let [{:keys [access-key secret-key]} config/qiniu
        interval-millsecond (* 1000 (Integer/parseInt config/work-interval-second))
        workers [worker/changelog-worker]]
    (qiniu/set-config! :access-key access-key
                       :secret-key secret-key)
    (work-forever workers
                  interval-millsecond)))
