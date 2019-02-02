(ns noted.events.search-view-events
  (:require [re-frame.core :as rf]
            [noted.events.events-utils :as eu]
            [clojure.string :as str]))


(rf/reg-event-db
  :search-view/update-search-query
  eu/default-interceptors
  (fn [db [qstr]]
    (assoc-in db [:search-view :query] qstr)))


(rf/reg-event-db
  :search-view/insert-tag-into-search
  eu/default-interceptors
  (fn [db [tag]]
    (let [curr-query (get-in db [:search-view :query])]
      (if (not (str/includes? curr-query (str "#" tag)))
        (assoc-in db [:search-view :query]
                  (str "#" tag " " curr-query))
        db))))