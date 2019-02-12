(ns noted.subs.sub-utils
  (:require [re-frame.core :as rf]
            [markdown.core :as md]
            [clojure.string :as str]))

(defn basic-sub [name path]
  (rf/reg-sub
    name
    (fn [db _]
      (get-in db path))))

(def md-important-chars "`~*_")


(defn close-char-maps [s]
  (str s (apply str (-> (fn [[last-accepted? stack] c]
                          (if (not (str/includes? md-important-chars c))
                            [false stack]
                            (if (and (= c (peek stack) (not last-accepted?)))
                              [false (pop stack)]
                              [true (conj stack c)])))
                        (reduce [false []] s)
                        (second)
                        (reverse)))))

(defn render-contents [note]
  (update note :content #(->> (take 500 %)
                              (apply str)
                              (close-char-maps)
                              (apply str)
                              (md/md->html))))