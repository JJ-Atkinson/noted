(ns noted.events.re-frame-gfsm-hooks
  (:require [re-frame.core :as rf]
            [noted.events.events-utils :as eu]
            [noted.events.global-state-fsm :as gfsm]))

(defn basic-hook [event-name fsm-transition]
  (rf/reg-event-fx
    event-name
    eu/default-interceptors
    (gfsm/views-fsm-> fsm-transition)))

(def event-mappings {:handle-esc                        :esc
                     :search-view/goto-view             :search-view
                     :search-view/goto-view-with-search :search-tag
                     :preview-note/goto-editor          :edit-preview
                     :preview-note/set-and-open-id      :view-note
                     :preview-note/pin                  :pin
                     :note-editor/submit-note-form      :submit})

(doall (map (fn [[name trans]] (basic-hook name trans)) event-mappings))