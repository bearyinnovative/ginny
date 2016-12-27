(ns ginny.config
  (:require [ginny.env :as env]))

(defonce platforms {:windows {:repo (env/->str :windows-repo "")
                              :changelog-path (env/->str :windows-changelog-path "")
                              :access-token (env/->str :windows-access-token "")}

                    :mac {:repo (env/->str :mac-repo "")
                          :changelog-path (env/->str :mac-changelog-path "")
                          :access-token (env/->str :mac-access-token "")}

                    :android {:repo (env/->str :android-repo "")
                              :changelog-path (env/->str :android-changelog-path "")
                              :access-token (env/->str :android-access-token "")}

                    :ios {:repo (env/->str :ios-repo "")
                          :changelog-path (env/->str :ios-changelog-path "")
                          :access-token (env/->str :ios-access-token "")}

                    :linux {:repo (env/->str :linux-repo "")
                            :changelog-path (env/->str :linux-changelog-path "")
                            :access-token (env/->str :linux-access-token "")}})

(defonce qiniu {:access-key (env/->str :qiniu-access-key "")
                :secret-key (env/->str :qiniu-secret-key "")
                :bucket (env/->str :qiniu-bucket "")})

(defonce incoming-hook-url (env/->str :incoming-hook-url ""))
