(ns electro-note.specs
  (:require [cljs.spec.alpha :as s]
            [re-frame.core :as rf]))



(defn check-and-throw
  "throw an exception if db doesn't match the spec"
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

;; Event handlers change state, that's their job. But what happens if there's
;; a bug which corrupts app state in some subtle way? This interceptor is run after
;; each event handler has finished, and it checks app-db against a spec.  This
;; helps us detect event handler bugs early.
(def check-spec-interceptor (rf/after #_identity (partial check-and-throw ::db-spec)))


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
                :search})

(s/def ::ui-common (s/keys :req-un [::mode
                                    ]))



(s/def ::db-spec (s/keys :req-un [::ui-common]
                         :opt-un []))


(def default-db {:ui-common {:mode                 :new-note}})

(rf/reg-event-db
  :reset-db
  [check-spec-interceptor]
  (fn [_ _]
    default-db))
