(ns noted.events.global-state-fsm
  (:require [noted.events.common-transitions :as ct]
            [noted.events.fsm :as fsm]
            [noted.events.search-view-events :as sve]
            [noted.events.preview-note-events :as pne]
            [noted.events.note-editor-events :as nee]
            [noted.events.fsm :as fsm
             :refer [compose unwrap-db]]
            [taoensso.timbre :as tmb]))


(def state-> (partial fsm/state-transition [:ui-common :mode]))

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
                                  ct/open-new-window)}
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
   :pinned       {:esc         ct/close-window
                  :pin         ct/close-window}})

; take a name of a transition and pass it through a fx handler
(def views-fsm-> (partial fsm/fx-handler machine [:db :ui-common :mode]))
