(ns noted.events.note-view-events
  (:require [re-frame.core :as rf]
            [noted.utils.common :as uc]
            [noted.specs :as specs]
            [taoensso.timbre :as tmb]
            [cljs.spec.alpha :as s]
            [noted.events.events-utils :as eu]))


(rf/reg-event-db
  :new-note-view/update-form-title
  eu/default-interceptors
  (fn [db [title]]
    (assoc-in db [:new-note-view :note-form :title] title)))

(rf/reg-event-db
  :new-note-view/update-form-content
  eu/default-interceptors
  (fn [db [content]]
    (assoc-in db [:new-note-view :note-form :content] content)))

(rf/reg-event-db
  :new-note-view/update-form-tags
  eu/default-interceptors
  (fn [db [tags-str]]
    (assoc-in db [:new-note-view :note-form :tags] tags-str)))



(rf/reg-event-db
  :new-note-view/submit-note
  eu/default-interceptors
  (fn [db []]
    (assoc-in db [:ui-common :notes]
              (let [note-form (-> db
                                  (get-in [:new-note-view :note-form])
                                  (update :tags uc/process-tag-str))
                    note-form-clean (dissoc note-form :id)
                    old-notes (get-in db [:ui-common :notes])]

                (if (not (s/valid? :noted.specs/note-form note-form))
                  (do (tmb/debug "did not submit form. didn't conform to ::note-form") old-notes)

                  (if (tmb/spy (= -1 (:id note-form)))
                    (conj old-notes note-form-clean)
                    (assoc old-notes (:id note-form) note-form-clean)))))))

(rf/reg-event-db
  :new-note-view/clear-form
  eu/default-interceptors
  (fn [db []]
    (assoc-in db [:new-note-view :note-form] specs/default-note-view-form)))


(rf/reg-event-fx
  :new-note-view/submit-note-form
  eu/default-interceptors
  (fn [{:keys [db]}]
    {:dispatch-n   [[:new-note-view/submit-note]
                    [:new-note-view/clear-form]]
     :hide-window  nil}))

(rf/reg-event-db
  :new-note-view/copy-note
  eu/default-interceptors
  (fn [db [id]]
    (assoc-in db [:new-note-view :note-form]
              (-> (get-in db [:ui-common :notes id])
                  (assoc :id id)
                  (update :tags uc/stringify-tags)))))

(rf/reg-event-fx
  :new-note-view/begin-editing
  eu/default-interceptors
  ; takes an id
  (fn [{:keys [event db]}]
    {:dispatch-n [[:new-note-view/copy-note (first event)]
                  [:set-active-mode :new-note]]}))


