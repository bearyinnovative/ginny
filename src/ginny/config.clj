(ns ginny.config
  (:require [clojure.java.io :as io])
  (:require [ginny.env :as env]
            [ginny.helper :as helper]))

(defonce changelog-config-path (env/->str :changelog-config ""))

(defonce qiniu {:access-key (env/->str :qiniu-access-key "")
                :secret-key (env/->str :qiniu-secret-key "")
                :base-url (env/->str :qiniu-base-url "")
                :pretty-base-url (env/->str :qiniu-pretty-base-url "")
                :bucket (env/->str :qiniu-bucket "")})

(defonce incoming-hook-url (env/->str :incoming-hook-url ""))

(defn get-changelog-platforms []
  (let [changelog-config (-> changelog-config-path
                        slurp
                        helper/json->map)]
    (:platforms changelog-config)))
