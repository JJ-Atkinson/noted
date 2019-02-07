(ns noted.specs
  (:require [cljs.spec.alpha :as s]
            [re-frame.core :as rf]
            [clojure.string :as str]
            [clojure.core]
            [taoensso.timbre :as tmb]))



(defn check-and-throw
  "throw an exception if db doesn't match the spec"
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (println (str "spec check failed: " (s/explain-str a-spec db)))))

;; Event handlers change state, that's their job. But what happens if there's
;; a bug which corrupts app state in some subtle way? This interceptor is run after
;; each event handler has finished, and it checks app-db against a spec.  This
;; helps us detect event handler bugs early.
(def check-spec-interceptor
  (rf/after (partial check-and-throw ::db-spec)))


(defn note-map? [note-map]
  (and
    (map? note-map)
    (every? true? (map (fn [[k v]]
                         (and (s/valid? ::note v)
                              (= k (:id v))))
                       note-map))))


(s/def ::mode #{:new-note
                :preview-note
                :search})

(defn not-whitespace? [s]
  (and (string? s)
       (not (empty? (str/trim s)))))


(s/def :note/title not-whitespace?)
(s/def :note/content not-whitespace?)
(s/def :note/tags (s/coll-of not-whitespace?))
(s/def :note/id int?)
(s/def ::note (s/keys :req-un [:note/title
                               :note/content
                               :note/tags
                               :note/id]
                      :opt-un []))


(s/def ::notes note-map?)

(s/def :note-form/tags not-whitespace?)
(s/def ::note-form (s/keys :req-un [
                                    :note/id
                                    :note/title
                                    :note/content
                                    :note-form/tags]
                           :opt-un []))

(s/def ::query string?)

(s/def ::search-view (s/keys :req-un [::query
                                      ]))

(s/def ::ui-common (s/keys :req-un [::mode
                                    ::notes
                                    ]))


(s/def ::note-editor (s/keys :req-un [::note-form]))

(s/def ::db-spec (s/keys :req-un [::note-editor
                                  ::ui-common
                                  ::search-view]
                         :opt-un []))


(s/def ::id int?)
(s/def ::preview-note (s/keys :req-un [::id]))

(def default-note-editor-form {:id      -1
                               :content ""
                               :title   ""
                               :tags    ""})

(def default-db {:preview-note {:id 0}
                 :note-editor  {:note-form default-note-editor-form}
                 :search-view  {:query ""}
                 :ui-common    {:mode  :new-note
                                :notes [#_{
                                           :title   "Where to locate lein"
                                           :content "`which lein`"
                                           :tags    ["bash" "tools"]}
                                        #_{
                                           :title   "Profiles in lein"
                                           :content "Profiles override arbitrary values in the base of 
          `defproject`. They can be used on any command with `lein with-profile [name] ...`"
                                           :tags    ["lein"]
                                           }
                                        ]}})


(rf/reg-event-db
  :reset-db
  [check-spec-interceptor]
  (fn [_ _]
    default-db))
 