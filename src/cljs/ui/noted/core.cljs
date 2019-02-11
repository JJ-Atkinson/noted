(ns noted.core
  (:require [reagent.core :as reagent :refer [atom]]
            [clojure.string :as string :refer [split-lines]]
            [re-frisk-remote.core :refer [enable-re-frisk-remote!]]
            [taoensso.timbre :as tmb]
            [noted.specs :as specs]
            [noted.events.events-utils :refer [e>]]
            [noted.events.events]
            [noted.subs.subs :refer [<s]]
            [noted.repl]
            [re-pressed.core :as rp]
            [noted.views.note-editor :as note-editor]
            [noted.views.search-view :as search-view]
            [noted.views.preview-note :as preview-note]
            [re-frame.core :as rf]
            [cljsjs.fuse]
            [re-frame.core :as re-frame]))

(def debug? true)

(enable-console-print!)

(def electron (js/require "electron"))
(def ipc (.-ipcRenderer electron))
(defn hide-self [] (.send ipc "message" ":hide"))
(defn dispatch-pull-req-from-main [] (.send ipc "message" ":pull"))
(defn dispatch-updated-notes [notes] (.send ipc "message" (str ":store" notes)))

;(defonce proc (js/require "child_process"))

;          js-args (clj->js (or args []))
;          p (.spawn proc cmd s-args)]
;      (.on p "error" (comp append-to-out
;                           #(str % "\n")))
;      (.on (.-stderr p) "data" append-to-out)
;      (.on (.-stdout p) "data" append-to-out))

(defn root-component []
  [:div#global-root.draggable-region
   (case (tmb/spy (<s [:active-mode]))
     :search-view [search-view/search-view]
     :note-editor [note-editor/note-editor]
     :preview-note [preview-note/preview-note]
     nil)])


(try
  (enable-re-frisk-remote! {:enable-re-frame-10x? true})
  (catch js/Error e
    nil))

(defn dev-setup []
  (enable-console-print!))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render
    [root-component]
    (js/document.getElementById "app")))

(defn init-main-comms []
  (.on ipc "message" (fn [_ new-mode]
                       (println "msg->ui" new-mode)
                       (e> [:receive-ipc-message new-mode])))
  (dispatch-pull-req-from-main))

(defn ^:export init []
  (rf/dispatch-sync [:reset-db])
  (rf/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])
  (dev-setup)
  (mount-root)
  (init-main-comms))

(re-frame/dispatch
  [::rp/set-keydown-rules
   {:event-keys [[[:handle-esc]
                  [{:keyCode 27}]]
                 [[:preview-note/goto-editor]
                  [{:keyCode 69                             ; E
                    :ctrlKey true}]]]
    ; esc
    :always-listen-keys [{:keyCode 27}]}])


; todo find a proper home for this. cant be in events because of a circular dep

(rf/reg-fx
  :hide-window
  (fn [_] (noted.core/hide-self)))
