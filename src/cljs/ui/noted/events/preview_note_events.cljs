(ns noted.events.preview-note-events
  (:require [re-frame.core :as rf]
            [noted.utils.common :as uc]
            [noted.specs :as specs]
            [taoensso.timbre :as tmb]
            [cljs.spec.alpha :as s]
            [noted.events.events-utils :as eu]))


(eu/basic-event :preview-note/set-id [:preview-note :id])


(rf/reg-event-fx
  :preview-note/set-and-open-id
  eu/default-interceptors
  (fn [{:keys [event db]}]
    {:dispatch-n [[:preview-note/set-id (first event)]
                  [:set-active-mode :preview-note]]}))

(rf/reg-event-fx
  :preview-note/goto-editor
  eu/default-interceptors
  (fn [{:keys [db]}]
    {:dispatch [:note-editor/begin-editing
                (get-in db [:preview-note :id])]}))

(rf/reg-event-fx
  :preview-note/maybe-edit
  eu/default-interceptors
  (fn [{:keys [db]}]
    (if (= :preview-note (get-in db [:ui-common :mode]))
      {:dispatch [:preview-note/goto-editor]}
      {})))

(rf/reg-event-fx
  :preview-note/search-for-tag
  eu/default-interceptors
  ; tag as text w/o the #
  (fn [{:keys [event db]}]
    {:dispatch-n [[:search-view/update-search-query (str "#" (first event))]
                  [:set-active-mode :search-view]}))

