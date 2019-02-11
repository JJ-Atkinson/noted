(ns noted.events.events
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as tmb]
            [noted.events.events-utils :as eu]
            [noted.events.fsm :as fsm
             :refer [compose unwrap-db]]
            [noted.events.preview-note-events]
            [noted.events.note-editor-events]
            [noted.events.search-view-events]
            [noted.events.re-frame-gfsm-hooks]))

; events is a simple file that composes all the other files below it. It should only
; contain top level behavior, and as often as possible should be shrunk.
; not all events will be incorparated into the fsm. Only view change events will be
; considered for the top level one. If views are complex enough, I may add additional 
; fsms in the future. It wouldn't be that hard at the moment


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


(rf/reg-fx
  :update-notes-fn
  (fn [notes] (noted.core/dispatch-updated-notes notes)))


(rf/reg-event-fx
  :dispatch-updated-notes
  eu/default-interceptors
  (fn [{:keys [db]}]
    {:update-notes-fn (get-in db [:ui-common :notes])}))

; ~~~~~~~~~~~~~~~~~~ end common ~~~~~~~~~~~~~~~~~~~~~~~
