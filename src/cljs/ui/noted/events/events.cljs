(ns noted.events.events
  (:require [noted.specs :as specs]
            [re-frame.core :as rf]
            [clojure.string :as str]
            [taoensso.timbre :as tmb]
            [cljs.spec.alpha :as s]
            [noted.events.note-view-events]
            [noted.events.search-view-events]
            [noted.events.note-viewer-events]
            [noted.utils.common :as uc]
            [noted.events.events-utils :as eu]))



(rf/reg-event-fx
  :receive-ipc-message
  eu/default-interceptors
  (fn [{:keys [event db]}]
    (let [parsed (cljs.reader/read-string (first event))
          current-mode (get-in db [:ui-common :mode])]
      (merge {:dispatch [:set-active-mode (:mode parsed)]}
             (when (and (= (:mode parsed) current-mode)
                                  (:visible? parsed))
               {:hide-window nil})))))

(rf/reg-event-db
  :set-active-mode
  eu/default-interceptors
  (fn [db [mode]]
    (assoc-in db [:ui-common :mode] mode)))





(rf/reg-fx
  :hide-window
  (fn [_] (noted.core/hide-self)))