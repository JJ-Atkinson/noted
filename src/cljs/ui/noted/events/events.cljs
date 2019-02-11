(ns noted.events.events
  (:require [noted.specs :as specs]
            [re-frame.core :as rf]
            [clojure.string :as str]
            [taoensso.timbre :as tmb]
            [cljs.spec.alpha :as s]
            [noted.utils.common :as uc]
            [noted.events.events-utils :as eu]
            [noted.events.search-view-events :as sve]
            [noted.events.note-editor-events :as nee]
            [noted.events.preview-note-events :as pne]
            [noted.events.fsm :as fsm
             :refer [compose unwrap-db]]
            [noted.events.common-transitions :as ct]))

; events is a simple file that composes all the other files below it. It should only
; contain top level behavior, and as often as possible should be shrunk.
; not all events will be incorparated into the fsm. Only view change events will be
; considered for the top level one. If views are complex enough, I may add fsms in the
; future. It wouldn't be that hard at the moment. 


(def state-> (partial fsm/state-transition  [:ui-common :mode]))

(def hide-transition (compose (state-> :hidden) ct/hide-window))



(def machine
  {:preview-note {:esc          hide-transition
                  :search-view  (state-> :search-view)
                  :note-editor  (state-> :note-editor)
                  :search-tag   (compose
                                  (state-> :search-view)
                                  sve/change-tag-search)
                  :edit-preview (compose
                                  (state-> :note-editor)
                                  pne/copy-preview-note-form)
                  :pin          (compose
                                  (state-> :pinned)
                                  ;(dispatch-main open-new)
                                  )}
   :search-view  {:search-view hide-transition
                  :note-editor (state-> :note-editor)
                  :view-note   (compose
                                 (state-> :preview-note)
                                 pne/change-preview-id)
                  :esc         (compose
                                 (state-> :hidden)
                                 ct/hide-window
                                 sve/clear-search-query)}
   :note-editor  {:note-editor hide-transition
                  :esc         (compose
                                 (state-> :hidden)
                                 ct/hide-window
                                 nee/clear-editor)
                  :submit      (compose
                                 (state-> :hidden)
                                 ct/hide-window
                                 nee/submit-note
                                 nee/clear-editor
                                 nee/dispatch-update-notes)
                  :search-view (state-> :search-view)}
   :hidden       {:search-view (state-> :search-view)
                  :note-editor (state-> :note-editor)}
   :pinned       {:esc nil #_(dispose-window)}
   })


(def views-fsm-> (partial fsm/fx-handler machine))

; these belong


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

(rf/reg-event-fx
  :handle-esc
  eu/default-interceptors
  (views-fsm-> :esc))


; ~~~~~~~~~~~~~~~~~~~ search-view ~~~~~~~~~~~~~~~~~~~~~~
; todo all of these events will be moved out asap.

(rf/reg-event-fx
  :search-view/goto-view
  eu/default-interceptors
  (views-fsm-> :search-view))

(rf/reg-event-fx
  :search-view/goto-view-with-search
  eu/default-interceptors
  (views-fsm-> :search-tag))


; ~~~~~~~~~~~~~~~~~~ preview-note ~~~~~~~~~~~~~~~~~~~~~


(rf/reg-event-fx
  :preview-note/goto-editor
  eu/default-interceptors
  (views-fsm-> :edit-preview))

(rf/reg-event-fx
  :preview-note/set-and-open-id
  eu/default-interceptors
  (views-fsm-> :view-note))

; ~~~~~~~~~~~~~~~~~~ note-editor ~~~~~~~~~~~~~~~~~~~~~~

(rf/reg-event-fx
  :note-editor/submit-note-form
  eu/default-interceptors
  (views-fsm-> :submit))