(ns noted.events.note-editor-events
  (:require [re-frame.core :as rf]
            [noted.utils.common :as uc]
            [noted.specs :as specs]
            [taoensso.timbre :as tmb]
            [cljs.spec.alpha :as s]
            [noted.events.fsm :refer [unwrap-db]]
            [noted.events.events-utils :as eu]))



(eu/basic-event :note-editor/update-form-title [:note-editor :note-form :title])
(eu/basic-event :note-editor/update-form-content [:note-editor :note-form :content])
(eu/basic-event :note-editor/update-form-tags [:note-editor :note-form :tags])


(def submit-note 
  (unwrap-db (fn [db]
   (let [note-form (-> db
                       (get-in [:note-editor :note-form])
                       (update :tags uc/process-tag-str))
         old-notes (get-in db [:ui-common :notes])
         id (if (= -1 (:id note-form))
              (eu/gen-id (keys (get-in db [:ui-common :notes])))
              (:id note-form))
         new-notes (assoc old-notes id (assoc note-form :id id))]
     
     (if (not (s/valid? :noted.specs/note note-form))
       (do (tmb/error
             "did not submit form, because it isn't conformal to ::note. "
             note-form)
           :fsm/failure)
       (assoc-in db [:ui-common :notes] new-notes))))))


(def clear-editor (unwrap-db 
  #(assoc-in % [:note-editor :note-form]
             specs/default-note-editor-form)))


(defn move-id-into-editor [db id]
  (assoc-in db [:note-editor :note-form]
            (-> (get-in db [:ui-common :notes id])
                (update :tags uc/stringify-tags))))

(defn dispatch-update-notes
  "take the current notes in memory and send them to the main proc for storage"
  [cofx collected-fx]
  (let [db-src (if (contains? collected-fx :db) collected-fx cofx)]
    (merge collected-fx {:update-notes-fn (get-in (:db db-src) [:ui-common :notes])})))