(ns noted.events.re-frame-gfsm-hooks
  (:require [re-frame.core :as rf]
            [noted.events.events-utils :as eu]
            [noted.events.global-state-fsm :as gfsm]))


(rf/reg-event-fx
  :handle-esc
  eu/default-interceptors
  (gfsm/views-fsm-> :esc))


; ~~~~~~~~~~~~~~~~~~~ search-view ~~~~~~~~~~~~~~~~~~~~~~
; todo all of these events will be moved out asap.

(rf/reg-event-fx
  :search-view/goto-view
  eu/default-interceptors
  (gfsm/views-fsm-> :search-view))

(rf/reg-event-fx
  :search-view/goto-view-with-search
  eu/default-interceptors
  (gfsm/views-fsm-> :search-tag))


; ~~~~~~~~~~~~~~~~~~ preview-note ~~~~~~~~~~~~~~~~~~~~~


(rf/reg-event-fx
  :preview-note/goto-editor
  eu/default-interceptors
  (gfsm/views-fsm-> :edit-preview))

(rf/reg-event-fx
  :preview-note/set-and-open-id
  eu/default-interceptors
  (gfsm/views-fsm-> :view-note))

(rf/reg-event-fx
  :preview-note/pin
  eu/default-interceptors
  (gfsm/views-fsm-> :pin))

; ~~~~~~~~~~~~~~~~~~ note-editor ~~~~~~~~~~~~~~~~~~~~~~

(rf/reg-event-fx
  :note-editor/submit-note-form
  eu/default-interceptors
  (gfsm/views-fsm-> :submit))