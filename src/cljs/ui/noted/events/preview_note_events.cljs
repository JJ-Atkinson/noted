(ns noted.events.preview-note-events
  (:require [re-frame.core :as rf]
            [noted.utils.common :as uc]
            [noted.specs :as specs]
            [taoensso.timbre :as tmb]
            [cljs.spec.alpha :as s]
            [noted.events.fsm :refer [unwrap-db]]
            [noted.events.note-editor-events :as nee]
            [noted.events.events-utils :as eu]
            [noted.events.fsm :as fsm]))



(defn change-preview-id
  "takes the :id from the event map and changes the preview to match"
  [cofx collected-fx]
  (-> collected-fx
      (assoc-in [:db :preview-note :id]
                (get-in cofx [:event 0]))
      (assoc-in [:db :preview-note :mode] :normal)))

(def copy-preview-note-form
  (unwrap-db #(nee/move-id-into-editor % (get-in % [:preview-note :id]))))

(def delete-self
  (unwrap-db (fn [db]
               (update-in db [:ui-common :notes] dissoc
                          (get-in db [:preview-note :id])))))


(def preview-state-> (partial fsm/state-transition [:preview-note :mode]))

(def preview-fsm
  {:normal {:delete (preview-state-> :deleting?)}
   :deleting? {:yes (fsm/compose
                      (preview-state-> :normal)
                      (fsm/state-transition [:ui-common :mode] :search-view)
                      delete-self
                      nee/dispatch-update-notes)
               :no    (preview-state-> :normal)}})

(def preview-state-fsm-> (partial fsm/fx-handler preview-fsm [:db :preview-note :mode]))

(rf/reg-event-fx
  :preview-note/delete-self
  eu/default-interceptors
  (preview-state-fsm-> :delete))

(rf/reg-event-fx
  :preview-note/delete-yes
  eu/default-interceptors
  (preview-state-fsm-> :yes))

(rf/reg-event-fx
  :preview-note/delete-no
  eu/default-interceptors
  (preview-state-fsm-> :no))


