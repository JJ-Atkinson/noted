(ns noted.events.events
  (:require [noted.specs :as specs]
            [re-frame.core :as rf]
            [clojure.string :as str]
            [taoensso.timbre :as tmb]
            [cljs.spec.alpha :as s]
            [noted.events.note-editor-events]
            [noted.events.search-view-events]
            [noted.events.preview-note-events]
            [noted.utils.common :as uc]
            [noted.events.events-utils :as eu]
            [noted.events.fsm :as fsm]))

(rf/reg-event-fx
  :receive-ipc-message
  eu/default-interceptors
  (fn [{:keys [event db]}]
    (let [parsed (cljs.reader/read-string (str (first event)))
          current-mode (get-in db [:ui-common :mode])]
      (cond
        (contains? parsed :mode)
        (merge {:dispatch [:set-active-mode (:mode parsed)]}
               (when (and (= (:mode parsed) current-mode)
                          (:visible? parsed))
                 {:hide-window nil}))

        (contains? parsed :store)
        {:dispatch [:update-note-store (:store parsed)]}

        :else
        (tmb/error "unexpected message: " parsed))
      )))


(eu/basic-event :update-note-store [:ui-common :notes])
(eu/basic-event :set-active-mode [:ui-common :mode])


(rf/reg-event-fx
  :handle-esc
  eu/default-interceptors
  (fn [{:keys [event db]}]
    (let [mode (tmb/spy (get-in db [:ui-common :mode]))]
      {:dispatch-n  [(when (= mode :note-editor) [:note-editor/clear-form])
                     (when (= mode :search-view [:search-view/update-search-query ""])]
       :hide-window nil})))

(rf/reg-event-fx
  :dispatch-updated-notes
  eu/default-interceptors
  (fn [{:keys [db]}]
    {:update-notes-fn (get-in db [:ui-common :notes])}))

(rf/reg-fx
  :hide-window
  (fn [_] (noted.core/hide-self)))

(rf/reg-fx
  :update-notes-fn
  (fn [notes] (noted.core/dispatch-updated-notes notes)))