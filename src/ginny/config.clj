(ns ginny.config
  (:require [ginny.env :as env]))

(defonce platforms {:windows {:enabled (env/->bool :windows-enabled false)
                              :repo (env/->str :windows-repo "")
                              :changelog-path (env/->str :windows-changelog-path "")
                              :access-token (env/->str :windows-access-token "")}

                    :mac {:enabled (env/->bool :mac-enabled false)
                          :repo (env/->str :mac-repo "")
                          :changelog-path (env/->str :mac-changelog-path "")
                          :access-token (env/->str :mac-access-token "")}

                    :android {:enabled (env/->bool :android-enabled false)
                              :repo (env/->str :android-repo "")
                              :changelog-path (env/->str :android-changelog-path "")
                              :access-token (env/->str :android-access-token "")}

                    :ios {:enabled (env/->bool :ios-enabled false)
                          :repo (env/->str :ios-repo "")
                          :changelog-path (env/->str :ios-changelog-path "")
                          :access-token (env/->str :ios-access-token "")}

                    :linux {:enabled (env/->bool :linux-enabled false)
                            :repo (env/->str :linux-repo "")
                            :changelog-path (env/->str :linux-changelog-path "")
                            :access-token (env/->str :linux-access-token "")}})

(defonce qiniu {:access-key (env/->str :qiniu-access-key "")
                :secret-key (env/->str :qiniu-secret-key "")
                :bucket (env/->str :qiniu-bucket "")})

(defonce incoming-hook-url (env/->str :incoming-hook-url ""))

(def enabled-platforms (->> platforms
                            (filter #(:enabled (second %)))
                            (map first)))
