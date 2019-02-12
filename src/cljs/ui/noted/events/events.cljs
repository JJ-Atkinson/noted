(ns noted.events.events
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as tmb]
            [noted.events.events-utils :as eu]
            [noted.events.fsm :as fsm :refer [compose unwrap-db]]
            [noted.events.preview-note-events]
            [noted.events.note-editor-events]
            [noted.events.search-view-events]
            [noted.events.re-frame-gfsm-hooks]
            [noted.events.global-state-fsm :as gfsm]))


(eu/basic-event :update-note-store [:ui-common :notes])
(eu/basic-event :set-active-mode [:ui-common :mode])

; events is a simple file that composes all the other files below it. It should only
; contain top level behavior, and as often as possible should be shrunk.
; not all events will be incorporated into the fsm. Only view change events will be
; considered for the top level one. If views are complex enough, I may add additional 
; fsms in the future.


(rf/reg-event-fx
  :receive-ipc-message
  eu/default-interceptors
  (fn [cofx]
    (let [parsed (cljs.reader/read-string (str (get-in cofx [:event 0])))]
      (condp #(contains? %2 %1) parsed
        :mode (fsm/invoke-fms gfsm/machine (:mode parsed) cofx)
        :store {:dispatch [:update-note-store (:store parsed)]}
        (tmb/error "unexpected message: " parsed)))))

(rf/reg-event-fx
  :dispatch-updated-notes
  eu/default-interceptors
  (fn [{:keys [db]}]
    {:update-notes-fn (get-in db [:ui-common :notes])}))

