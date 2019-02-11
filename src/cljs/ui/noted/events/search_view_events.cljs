(ns noted.events.search-view-events
  (:require [re-frame.core :as rf]
            [noted.events.events-utils :as eu]
            [clojure.string :as str]
            [noted.events.fsm :refer [unwrap-db]]
            [taoensso.timbre :as tmb]))



(eu/basic-event :search-view/update-search-query [:search-view :query])

(rf/reg-event-db
  :search-view/insert-tag-into-search
  eu/default-interceptors
  (fn [db [tag]]
    (let [curr-query (get-in db [:search-view :query])]
      (if (not (str/includes? curr-query (str "#" tag)))
        (assoc-in db [:search-view :query]
                  (str "#" tag " " curr-query))
        db))))




(defn change-tag-search [cofx collected-fx]
  (assoc-in collected-fx [:db :search-view :query] (get-in cofx [:event 0])))

(def clear-search-query (unwrap-db #(assoc-in % [:search-view :query] "")))

