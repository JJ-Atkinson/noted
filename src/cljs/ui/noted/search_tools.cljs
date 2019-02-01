(ns noted.search-tools
  (:require [clojure.string :as str]
            [taoensso.timbre :as tmb]
            [noted.utils.common :as uc]
            [medley.core :as mc]
            [clojure.set :as cset]
            [sc.api :as scc]))

(defn clear-first-char [s]
  (str/join "" (rest s)))

(def tag-regex #"#[\S]+")

(defn grab-tags [query]
  (re-seq tag-regex query))

(defn clear-tags [query]
  (str/replace query tag-regex ""))

(defn keep-sorted [notes ids]
  (map #(get notes %) ids))

(defn fuse-search [clear-query notes js-notes]
  (let [opts {:keys     [{:name   "title"
                          :weight 0.6}
                         {:name   "content"
                          :weight 0.4}]
              :tokenize true
              :id       "id"}
        fuse (js/Fuse. js-notes (clj->js opts))
        ids (js->clj (.search fuse clear-query))]
    (keep-sorted notes ids)))

(defn filter-by-tags [notes tags]
  (filter (fn [n]
            (uc/any-? true? (for [rtag (:tags n)
                                  ptag tags]
                              (str/starts-with? rtag ptag))))
          notes))

(defn search [query notes js-notes]
  (let [tags (map clear-first-char (grab-tags query))
        clean-query (str/trim (clear-tags query))
        text-results (if (empty? clean-query)
                       notes
                       (fuse-search clean-query notes js-notes))
        tag-results (if (empty? tags)
                      text-results
                      (filter-by-tags text-results tags))]
    tag-results))
