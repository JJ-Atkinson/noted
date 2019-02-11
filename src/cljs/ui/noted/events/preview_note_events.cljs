(ns noted.events.preview-note-events
  (:require [re-frame.core :as rf]
            [noted.utils.common :as uc]
            [noted.specs :as specs]
            [taoensso.timbre :as tmb]
            [cljs.spec.alpha :as s]
            [noted.events.fsm :refer [unwrap-db]]
            [noted.events.events-utils :as eu]))



(defn change-preview-id
  "takes the :id from the event map and changes the preview to match"
  [cofx collected-fx]
  (assoc-in collected-fx [:db :preview-note :id]
            (get-in cofx [:event 0])))


; todo belongs in note-editor
(defn move-id-into-editor [db id]
  (assoc-in db [:note-editor :note-form]
            (-> (get-in db [:ui-common :notes id])
                (update :tags uc/stringify-tags))))

(def copy-preview-note-form 
  (unwrap-db #(move-id-into-editor % (get-in % [:preview-note :id]))))
