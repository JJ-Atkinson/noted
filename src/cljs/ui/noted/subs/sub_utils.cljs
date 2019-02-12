(ns noted.subs.sub-utils
  (:require [re-frame.core :as rf]
            [markdown.core :as md]
            [clojure.string :as str]
            [noted.utils.common :as uc]
            [taoensso.timbre :as tmb]))

(defn basic-sub [name path]
  (rf/reg-sub
    name
    (fn [db _]
      (get-in db path))))

(def md-important-char-map {"`" "`", "~" "~", "*" "*", "_" "_", "(" ")", "[" "]"})

(defn closing-chars-of [s]
  (apply str (-> (fn [[last-accepted? stack] c]
                   (let [opening-char? (contains? md-important-char-map c)
                         closing-char? (= c (peek stack))
                         add-matching [true (conj stack (get md-important-char-map c))]]
                     (if closing-char?
                       (if last-accepted?
                         add-matching
                         [false (pop stack)])
                       (if opening-char?
                         add-matching
                         [false stack]))))
                 
                 (reduce [false []] s)
                 (second)
                 (reverse))))

(defn close-char-maps [s]
  (str s "" (str/replace (closing-chars-of s) "```" "```\n")))

(defn render-contents [note]
  (update note :content #(md/md->html
                           (tmb/spy (str (apply str (->> (take 500 %)
                                                         (apply str)
                                                         (close-char-maps)))
                                         (when (< 500 (count %))
                                           " **`...`**"))))))