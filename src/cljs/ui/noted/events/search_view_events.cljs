(ns noted.events.search-view-events
  (:require [re-frame.core :as rf]
            [noted.events.events-utils :as eu]))


(rf/reg-event-db
  :search-view/update-search-query
  eu/default-interceptors
  (fn [db [qstr]]
    (assoc-in db [:search-view :query] qstr)))


(rf/reg-event-db
  :search-view/insert-tag-into-search
  eu/default-interceptors
  (fn [db [tag]]
    (assoc-in db [:search-view :query]
              (str "#" tag " "
                   (get-in db [:search-view :query])))))