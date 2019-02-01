(ns noted.events.note-viewer-events
  (:require [re-frame.core :as rf]
            [noted.utils.common :as uc]
            [noted.specs :as specs]
            [taoensso.timbre :as tmb]
            [cljs.spec.alpha :as s]
            [noted.events.events-utils :as eu]))


(rf/reg-event-db
  :note-viewer/set-id
  eu/default-interceptors
  (fn [db [id]]
    (assoc-in db [:note-viewer :id] id)))


(rf/reg-event-fx
  :note-viewer/goto-id
  eu/default-interceptors
  (fn [{:keys [event db]}]
    {:dispatch-n [[:note-viewer/set-id (first event)]
                  [:set-active-mode :note-viewer]]}))

(rf/reg-event-fx
  :note-viewer/goto-editor
  eu/default-interceptors
  (fn [{:keys [db]}]
    {:dispatch [:new-note-view/begin-editing
                (get-in db [:note-viewer :id])]}))

(rf/reg-event-fx
  :note-viewer/search-for-tag
  eu/default-interceptors
  ; tag as text w/o the #
  (fn [{:keys [event db]}]
    {:dispatch-n [[:search-view/update-search-query (str "#" (first event))]
                  [:set-active-mode :search]]}))

