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
                                    (let [id (cljs.reader/read-string n)]
                                      (if (contains? notes id)
                                        (basic-render (get notes id))
                                        "\n\n`{{{Missing Note}}}`\n\n")))))
      ) notes))