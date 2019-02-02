(ns noted.specs
  (:require [cljs.spec.alpha :as s]
            [re-frame.core :as rf]
            [clojure.string :as str]))



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


#_(s/def ::tracks-data (s/coll-of :lib-spec/track-cleaned))
#_(s/def ::album-data-client (s/merge (s/keys
                                        :req-un [::album-art-url
                                                 ::tracks-data])
                                      :lib-spec/album))
#_(s/def ::active-view #{:artist-view
                         :album-view
                         :search-view
                         :tracks-view})

(s/def ::mode #{:new-note
                :note-viewer
                :search})

(defn not-whitespace [s]
  (and (string? s)
       (not (empty? (str/trim s)))))


(s/def :note/title string?)
(s/def :note/content string?)
(s/def :note/tags (s/coll-of string?))
(s/def ::note (s/keys :req-un [:note/title
                               :note/content
                               :note/tags]
                      :opt-un []))


(s/def ::notes (s/coll-of ::note))

(s/def :note-form/title not-whitespace)
(s/def :note-form/content not-whitespace)
(s/def :note-form/tags (partial s/valid? :note/tags))
(s/def ::note-form (s/keys :req-un [:note-form/title
                                    :note-form/content
                                    :note-form/tags]
                           :opt-un []))

(s/def ::query string?)

(s/def ::search-view (s/keys :req-un [::query
                                      ]))

(s/def ::ui-common (s/keys :req-un [::mode
                                    ::notes
                                    ]))


(s/def ::new-note-view (s/keys :req-un []))

(s/def ::db-spec (s/keys :req-un [::new-note-view
                                  ::ui-common
                                  ::search-view]
                         :opt-un []))


(s/def ::id int?)
(s/def ::note-viewer (s/keys :req-un [::id]))

(def default-note-view-form {:id -1
                             :content ""
                             :title ""
                             :tags ""})

(def default-db {:note-viewer   {:id 0}
                 :new-note-view {:note-form default-note-view-form}
                 :search-view   {:query ""}
                 :ui-common     {:mode  :new-note
                                 :notes [{
                                          :title   "Where to locate lein"
                                          :content "`which lein`"
                                          :tags    ["bash" "tools"]}
                                         {
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
 