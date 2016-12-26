(defproject ginny"0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Unlicense"
            :url "http://unlicense.org/UNLICENSE"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.6.3"]
                 [environ "1.1.0"]
                 [clj-http "2.3.0"]
                 [clj.qiniu "0.1.2"]
                 [com.taoensso/timbre "4.8.0"]]
  :main ^:skip-aot ginny.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
