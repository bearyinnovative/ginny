(ns ginny.changelog-test
  (:require [ginny.changelog :as sut]
            [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.data]
            [taoensso.timbre :as timbre]))

(deftest test-parse-kv-item
  (let [parse #(sut/parse-kv-item %)]
    (testing "parse good kv list"
      (let [md-line "- Name: bearychat"
            kv (parse md-line)]
        (is (not (nil? kv)))
        (is (= (:name kv) "bearychat"))))
    (testing "parse bad kv list"
      (let [md-line "- Name"
            kv (parse md-line)]
        (is (nil? kv))))))

(deftest test-parse-header
  (let [parse #(sut/parse-header %)]
    (testing "parse good header"
      (let [header (str "- Name: bearychat\n"
                        "- Platform: MAC\n")
            kvs (parse header)]
        (is (= (count kvs) 2))
        (is (= (:name kvs) "bearychat"))
        (is (= (:platform kvs) "MAC"))))
    (testing "parse bad header"
      (let [header (str "- Name:\n"
                        "- Platform: Mac\n")
            kvs (parse header)]
        (is (= (count kvs) 1))
        (is (= (:platform kvs) "Mac"))))))

(deftest test-sub-category?
  (testing "check sub category"
    (let [md (str " ### 32bit\n"
                  " - abc\n"
                  "- efg\n")]
      (is (sut/sub-category? md))))
  (testing "check item"
    (let [md (str "- abc\n"
                  " - efg\n")]
      (is (not (sut/sub-category? md))))))

(deftest test-parse-category-title
  (let [parse #(sut/parse-category-title %)]
    (testing "parse good title"
      (let [md-line " Fixed  "
            title (parse md-line)]
        (is (= title "Fixed"))))
    (testing "parse bad title"
      (let [md-line " Fixed items"
            title (parse md-line)]
        (is (nil? title))))))

(deftest test-parse-category
  (let [parse #(sut/parse-category %)]
    (testing "parse category with items"
      (let [md (str "Fixed\n"
                    "- fixed a bug\n"
                    "- fixed another bug")
            category (parse md)
            items (:fixed category)]
        (is (= (count category) 1))
        (is (= (count items) 2))
        (is (= (first items) "fixed a bug"))
        (is (= (second items) "fixed another bug"))))
    (testing "parse category with sub-category"
      (let [md (string/join \newline ["Client"
                                      "### 32bit"
                                      "- Size: 20mb"
                                      "- DownloadUrl: http://bearychat.com/32bit.exe"
                                      "### 64bit"
                                      "- Size: 30mb"
                                      "- DownloadUrl: http://bearychat.com/64bit.exe"])
            category (parse md)]
        (is (= (count category) 1))
        (is (= (count (:client category)) 2))
        (is (= (-> category
                   :client
                   :32bit
                   :size) "20mb"))
        (is (= (-> category
                   :client
                   :32bit
                   :downloadurl) "http://bearychat.com/32bit.exe"))
        (is (= (-> category
                   :client
                   :64bit
                   :size) "30mb"))
        (is (= (-> category
                   :client
                   :64bit
                   :downloadurl) "http://bearychat.com/64bit.exe"))))))

(deftest test-parse-release-basic-info
  (let [parse #(sut/parse-release-basic-info %)]
    (testing "parse good basic info"
      (let [md-line "  2.1.0 / 2016-12-24 "
            info (parse md-line)]
        (is (= (count info) 2))
        (is (= (:version info) "2.1.0"))
        (is (= (:date info) "2016-12-24"))))
    (testing "parse bad basic info"
      (let [md-line "  2.1.0 2016-12-24 "
            info (parse md-line)]
        (is (nil? info))))))

(deftest test-parse-release
  (let [parse #(sut/parse-release %)]
    (testing "parse good release"
      (let [md (string/join \newline [" 2.1.0 / 2016-12-24"
                                      "## Client"
                                      "### 32bit"
                                      "- Size: 20mb"
                                      "- DownloadUrl: http://bearychat.com/32bit.exe"
                                      "### 64bit"
                                      "- Size: 30mb"
                                      "- DownloadUrl: http://bearychat.com/64bit.exe"
                                      "## Fixed"
                                      "- Fixed a bug"
                                      "- Fixed another bug"
                                      "## Added"
                                      "- Added a feature"
                                      "- Added another feature"
                                      "- Added third feature"])
            release (parse md)]
        (is (= {:version "2.1.0"
                :date "2016-12-24"
                :client {:32bit {:size "20mb"
                                 :downloadurl "http://bearychat.com/32bit.exe"}
                         :64bit {:size "30mb"
                                 :downloadurl "http://bearychat.com/64bit.exe"}}
                :fixed '("Fixed a bug"
                         "Fixed another bug")
                :added '("Added a feature"
                         "Added another feature"
                         "Added third feature")} release))))))

(deftest test-parse-body
  (let [parse #(sut/parse-body %)]
    (testing "parse good body"
      (let [md (string/join \newline ["# 2.1.0 / 2016-12-24"
                                      "## Client"
                                      "### 32bit"
                                      "- Size: 20mb"
                                      "- DownloadUrl: http://bearychat.com/32bit.exe"
                                      "### 64bit"
                                      "- Size: 30mb"
                                      "- DownloadUrl: http://bearychat.com/64bit.exe"
                                      "## Fixed"
                                      "- Fixed a bug"
                                      "- Fixed another bug"
                                      "## Added"
                                      "- Added a feature"
                                      "- Added another feature"
                                      "- Added third feature"
                                      "# 2.0.0 / 2016-11-24"
                                      "## Client"
                                      "### 32bit"
                                      "- Size: 50mb"
                                      "- DownloadUrl: http://bearychat.com/old/32bit.exe"
                                      "### 64bit"
                                      "- Size: 40mb"
                                      "- DownloadUrl: http://bearychat.com/old/64bit.exe"
                                      "## Fixed"
                                      "- Fixed a bug1"
                                      "## Added"
                                      "- Added a feature1"
                                      "- Added another feature2"])
            body (parse md)]
        (is (= (count (:releases body)) 2))
        (is (= (first (:releases body)) {:version "2.1.0"
                                         :date "2016-12-24"
                                         :client {:32bit {:size "20mb"
                                                          :downloadurl "http://bearychat.com/32bit.exe"}
                                                  :64bit {:size "30mb"
                                                          :downloadurl "http://bearychat.com/64bit.exe"}}
                                         :fixed '("Fixed a bug"
                                                  "Fixed another bug")
                                         :added '("Added a feature"
                                                  "Added another feature"
                                                  "Added third feature")}))
        (is (= (second (:releases body)) {:version "2.0.0"
                                          :date "2016-11-24"
                                          :client {:32bit {:size "50mb"
                                                           :downloadurl "http://bearychat.com/old/32bit.exe"}
                                                   :64bit {:size "40mb"
                                                           :downloadurl "http://bearychat.com/old/64bit.exe"}}
                                          :fixed '("Fixed a bug1")
                                          :added '("Added a feature1"
                                                   "Added another feature2")}))))))

(deftest test-parse-changelog
  (let [parse #(sut/parse-changelog %)]
    (testing "parse good changelog"
      (let [md (string/join \newline ["----"
                                      "- Name: bearychat"
                                      "- Platform: MAC"
                                      ""
                                      "----"
                                      ""
                                      "# 2.1.0 / 2016-12-24"
                                      "## Client"
                                      "### 32bit"
                                      "- Size: 20mb"
                                      "- DownloadUrl: http://bearychat.com/32bit.exe"
                                      ""
                                      "### 64bit"
                                      "- Size: 30mb"
                                      "- DownloadUrl: http://bearychat.com/64bit.exe"
                                      ""
                                      "## Fixed"
                                      "- Fixed a bug"
                                      "- Fixed another bug"
                                      ""
                                      "## Added"
                                      "- Added a feature"
                                      "- Added another feature"
                                      "- Added third feature"
                                      ""
                                      "## Cover"
                                      "[![hello](http://bearychat.com/static/cover.png)](http://bearychat.com)"
                                      ""
                                      "# 2.0.0 / 2016-11-24"
                                      "## Client"
                                      "### 32bit"
                                      "- Size: 50mb"
                                      "- DownloadUrl: http://bearychat.com/old/32bit.exe"
                                      ""
                                      "### 64bit"
                                      "- Size: 40mb"
                                      "- DownloadUrl: http://bearychat.com/old/64bit.exe"
                                      ""
                                      "## Fixed"
                                      "- Fixed a bug1"
                                      ""
                                      "## Added"
                                      "- Added a feature1"
                                      "- Added another feature2"
                                      ""
                                      "## Cover"
                                      "![](http://static.qiniu.com/png/123sa.png)"])
            changelog (parse md)]
        (is (= (count changelog) 2))
        (is (= (:header changelog) {:name "bearychat"
                                    :platform "MAC"}))
        (is (= (-> changelog
                   :body
                   :releases
                   count) 2))
        (is (= (-> changelog
                   :body
                   :releases
                   first) {:version "2.1.0"
                           :date "2016-12-24"
                           :client {:32bit {:size "20mb"
                                            :downloadurl "http://bearychat.com/32bit.exe"}
                                    :64bit {:size "30mb"
                                            :downloadurl "http://bearychat.com/64bit.exe"}}
                           :fixed '("Fixed a bug"
                                    "Fixed another bug")
                           :added '("Added a feature"
                                    "Added another feature"
                                    "Added third feature")
                           :cover '{:link "http://bearychat.com"
                                    :image_text "hello"
                                    :image_url "http://bearychat.com/static/cover.png"}}))
        (is (= (-> changelog
                   :body
                   :releases
                   second) {:version "2.0.0"
                            :date "2016-11-24"
                            :client {:32bit {:size "50mb"
                                             :downloadurl "http://bearychat.com/old/32bit.exe"}
                                     :64bit {:size "40mb"
                                             :downloadurl "http://bearychat.com/old/64bit.exe"}}
                            :fixed '("Fixed a bug1")
                            :added '("Added a feature1"
                                     "Added another feature2")
                            :cover '{:link nil
                                      :image_text ""
                                      :image_url "http://static.qiniu.com/png/123sa.png"}}))))))
