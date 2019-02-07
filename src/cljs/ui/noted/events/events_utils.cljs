(ns noted.events.events-utils
  (:require [re-frame.core :as rf]
            [noted.specs :as specs]))


(def e> rf/dispatch)

(def default-interceptors [specs/check-spec-interceptor
                           rf/trim-v])

(defn gen-id [curr-ids]
  (inc (apply max curr-ids)))

(defn basic-event [name path]
  (rf/reg-event-db
    name
    default-interceptors
    (fn [db [e]] (assoc-in db path e))))
