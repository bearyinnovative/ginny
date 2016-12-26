(ns ginny.changelog
  (:require [clojure.string :as string]
            [ginny.helper :as h]
            [taoensso.timbre :as timbre]))

(defn parse-kv-item
  [md-line]
  (let [re #"^\s*-\s*(\w+)\s*:\s*(.+)\s*$"
        [match key value] (re-find re md-line)]
    (when-not (nil? match)
      {(h/->keyword key) value})))

(defn parse-header
  [md]
  (let [lines (h/split-lines md)
        kv-list (->> lines
                     (map parse-kv-item)
                     (reduce merge))]
    kv-list))

(defn sub-category?
  [md]
  (let [re #"^\s*#{3}\s*"
        match (re-find re md)]
    (not (nil? match))))

(defn kv-item?
  [md-line]
  (let [re #"^\s*\w+\s*:.+"
        match (re-find re md-line)]
    (not (nil? match))))

(defn parse-category-title
  [md-line]
  (when (string? md-line)
    (second (re-find #"^\s*(\w+)\s*$" md-line))))

(declare parse-category)

(defn parse-category-with-sub-categories
  [md]
  (let [sub-category-mds (h/split (str \newline md)
                                  #"\n\s*###\s*")]
    (->> sub-category-mds
         (map parse-category)
         (reduce merge))))

(defn parse-category-with-items
  [md]
  (let [item-mds (h/split (str \newline md)
                          #"\n\s*-\s*")]
    (if (kv-item? (first item-mds))
      (->> (h/split-lines md)
           (map parse-kv-item)
           (reduce merge))
      item-mds)))

(defn parse-category
  [md]
  (let [[title-md child-md] (h/split-lines md 2)
        title (parse-category-title title-md)]
    (when-not (nil? title)
      (if (sub-category? child-md)
        (let [sub-categories (parse-category-with-sub-categories child-md)]
          {(h/->keyword title) sub-categories})
        (let [items (parse-category-with-items child-md)]
          {(h/->keyword title) items})))))

(defn parse-release-basic-info
  [md-line]
  (let [re #"^\s*(.+)\s*/\s*(.+)\s*$"
        [match version date] (re-find re md-line)]
    (when-not (nil? match)
      {:version (string/trim version)
       :date (string/trim date)})))

(defn parse-release
  [md]
  (let [[basic-info-md categories-md] (h/split-lines md 2)
        category-mds (h/split (str \newline categories-md)
                              #"\n\s*##(?!#)\s*")
        basic-info (parse-release-basic-info basic-info-md)
        categories (->> category-mds
                        (map parse-category)
                        (reduce merge))]
    (when-not (nil? basic-info)
      (merge basic-info categories))))

(defn parse-body
  [md]
  (let [release-mds (h/split (str \newline md)
                             #"\n\s*#(?!#)\s*")
        releases (map parse-release release-mds)]
    {:releases releases}))

(defn before-parse
  [md]
  (string/replace md #"\n+" "\n"))

(defn parse-changelog
  [md]
  (let [[header-md body-md] (h/split (before-parse md)
                                     #"\n-{4,}")
        header (parse-header header-md)
        body (parse-body body-md)]
    {:header header
     :body body}))

(defn get-platform
  [changelog]
  (-> changelog
      :header
      :platform))
