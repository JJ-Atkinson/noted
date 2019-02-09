(ns noted.note-insertion
  (:require [medley.core :as mc]
            [clojure.string :as str]
            [taoensso.timbre :as tmb]))

(defn syntax-for [id]
  (str "{{{" id "}}}"))



(defn basic-render [n]
  (str "## " (:title n) "\n\n**`" (syntax-for (:id n)) "`**\n\n" (:content n)))

(defn process-insertion [notes]
  (mc/map-vals
    (fn [n] (update n :content
                    #(str/replace %
                                  #"\{\{\{(\d+)\}\}\}"
                                  (fn [[_ n]]
                                    (basic-render (get notes (cljs.reader/read-string n))))))
      ) notes))