(ns electro-note.events.events
  (:require [electro-note.specs :as specs]
            [re-frame.core :as rf]))

(rf/reg-event-db
  :set-active-mode
  [specs/check-spec-interceptor]
  (fn [db [_ mode]]
    (assoc-in db [:ui-common :mode] mode)))