(ns noted.events.note-editor-events
  (:require [re-frame.core :as rf]
            [noted.utils.common :as uc]
            [noted.specs :as specs]
            [taoensso.timbre :as tmb]
            [cljs.spec.alpha :as s]
            [noted.events.events-utils :as eu]))


(defn gen-id [curr-ids]
  (inc (apply max curr-ids)))


(rf/reg-event-db
  :note-editor/update-form-title
  eu/default-interceptors
  (fn [db [title]]
    (assoc-in db [:note-editor :note-form :title] title)))

(rf/reg-event-db
  :note-editor/update-form-content
  eu/default-interceptors
  (fn [db [content]]
    (assoc-in db [:note-editor :note-form :content] content)))

(rf/reg-event-db
  :note-editor/update-form-tags
  eu/default-interceptors
  (fn [db [tags-str]]
    (assoc-in db [:note-editor :note-form :tags] tags-str)))



(rf/reg-event-db
  :note-editor/submit-note
  eu/default-interceptors
  (fn [db []] 
    (assoc-in db [:ui-common :notes]
              (let [note-form (get-in db [:note-editor :note-form])
                    old-notes (get-in db [:ui-common :notes])
                    id (if (= -1 (:id note-form))
                         (gen-id (keys (get-in db [:ui-common :notes])))
                         (:id note-form))]
                (if (not (s/valid? :noted.specs/note-form note-form))
                  (do (tmb/error "did not submit form. didn't conform to ::note-form. they are " note-form) old-notes)
                  (assoc old-notes id
                    (update note-form :tags uc/process-tag-str)))))))

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


