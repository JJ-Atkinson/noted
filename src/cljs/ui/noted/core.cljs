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
            [noted.views.new-note-view :as new-note-view]
            [noted.views.search-view :as search-view]
            [noted.views.note-viewer :as note-viewer]
            [re-frame.core :as rf]
            [cljsjs.fuse]
            [re-frame.core :as re-frame]))

(enable-console-print!)

(def electron (js/require "electron"))
(def ipc (.-ipcRenderer electron))
(defn hide-self [] (.send ipc "message" ":hide"))

;(defonce proc (js/require "child_process"))

;          js-args (clj->js (or args []))
;          p (.spawn proc cmd s-args)]
;      (.on p "error" (comp append-to-out
;                           #(str % "\n")))
;      (.on (.-stderr p) "data" append-to-out)
;      (.on (.-stdout p) "data" append-to-out))

(defn root-component []
  [:div#global-root.draggable-region
   (case (<s [:active-mode])
     :search [search-view/search-view]
     :new-note [new-note-view/new-note-view]
     :note-viewer [note-viewer/note-viewer])])


(enable-re-frisk-remote! {:enable-re-frame-10x? true})

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
                       (e> [:receive-ipc-message new-mode]))))

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
                 [[:note-viewer/maybe-edit]
                  [{:keyCode 69                             ; E
                    :ctrlKey true}]]]
    ; esc
    :always-listen-keys [{:keyCode 27}]}])
