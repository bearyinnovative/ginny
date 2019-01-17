(ns ginny.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string])
  (:require [ginny.config :as config]
            [ginny.workers.changelog :as changelog]
            [ginny.storages.qiniu :as qiniu])
  (:gen-class))

(defn- run-workers
  [workers]
  ; TODO: run in parallel
  (run! #(%) workers))

(defn build-and-upload
  []
  (let [{:keys [access-key secret-key]} config/qiniu
        workers [changelog/build-and-upload]]
    (qiniu/init-config access-key secret-key)
    (run-workers workers)))

(defn build-only
  [& {:as kwargs}]
  (changelog/build-all-changelogs kwargs))

(defn help
  []
  (println (string/join \newline
                        ["USAGE:"
                         "    ginny <SUBCOMMAND>"
                         ""
                         "SUBCOMMANDS:"
                         "    build  -  build json files only"
                         "    run    -  build json files and upload/rsync"
                         "    hlep   -  print this info"])))

(def cli-options
  [["-o" "--output output" :default "./"]])

(defn -main
  "I don't do a whole lot."
  [& args]
  (let [{:keys [options arguments]} (parse-opts args cli-options)]
    (case (first arguments)
      "build"
      (build-only :prefix (:output options))

      "run"
      (build-and-upload)

      "help"
      (help)

      (help))))

