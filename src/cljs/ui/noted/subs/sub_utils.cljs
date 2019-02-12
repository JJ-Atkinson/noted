(ns noted.subs.sub-utils
  (:require [re-frame.core :as rf]
            [markdown.core :as md]))

(defn basic-sub [name path]
  (rf/reg-sub
    name
    (fn [db _]
      (get-in db path))))

(def md-important-chars "`")

(defn close-char-maps [])

(defn render-contents [note]
  (update note :content #(md/md->html (apply str (take 500 %)))))