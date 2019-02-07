(ns noted.events.note-editor-events
  (:require [re-frame.core :as rf]
            [noted.utils.common :as uc]
            [noted.specs :as specs]
            [taoensso.timbre :as tmb]
            [cljs.spec.alpha :as s]
            [noted.events.events-utils :as eu]))





(eu/basic-event :note-editor/update-form-title  [:note-editor :note-form :title])
(eu/basic-event :note-editor/update-form-content [:note-editor :note-form :content])
(eu/basic-event :note-editor/update-form-tags [:note-editor :note-form :tags])



(rf/reg-event-db
  :note-editor/submit-note
  eu/default-interceptors
  (fn [db []] 
    (assoc-in db [:ui-common :notes]
              (let [note-form (get-in db [:note-editor :note-form])
                    old-notes (get-in db [:ui-common :notes])
                    id (if (= -1 (:id note-form))
                         (eu/gen-id (keys (get-in db [:ui-common :notes])))
                         (:id note-form))]
                (if (not (s/valid? :noted.specs/note-form note-form))
                  (do (tmb/error "did not submit form, because it isn't conformal to ::note-form. " note-form) old-notes)
                  (assoc old-notes id
                                   (-> note-form
                                       (update :tags uc/process-tag-str)
                                       (assoc :id id))))))))

(rf/reg-event-db
  :note-editor/clear-form
  eu/default-interceptors
  (fn [db []]
    (assoc-in db [:note-editor :note-form] specs/default-note-editor-form)))


(rf/reg-event-fx
  :note-editor/submit-note-form
  eu/default-interceptors
  (fn [{:keys [db]}]
    {:dispatch-n             [[:note-editor/submit-note]
                              [:note-editor/clear-form]
                              [:dispatch-updated-notes]]
     :hide-window            nil}))

(rf/reg-event-db
  :note-editor/copy-note
  eu/default-interceptors
  (fn [db [id]]
    (assoc-in db [:note-editor :note-form]
              (-> (get-in db [:ui-common :notes id])
                  (update :tags uc/stringify-tags)))))

(rf/reg-event-fx
  :note-editor/begin-editing
  eu/default-interceptors
  ; takes an id
  (fn [{:keys [event db]}]
    {:dispatch-n [[:note-editor/copy-note (first event)]
                  [:set-active-mode :new-note]]}))


