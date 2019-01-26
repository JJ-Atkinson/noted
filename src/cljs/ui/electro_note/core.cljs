(ns electro-note.core
  (:require [reagent.core :as reagent :refer [atom]]
            [clojure.string :as string :refer [split-lines]]
            [re-frisk-remote.core :refer [enable-re-frisk-remote!]]
            [taoensso.timbre :as tmb]
            [electro-note.specs :as specs]
            [electro-note.events.events]
            [electro-note.subs.subs]
            [re-pressed.core :as rp]
            [electro-note.views.new-note-view :as note-view]
            [electro-note.views.search-view :as search-view]
            [re-frame.core :as rf]))


(enable-console-print!)



(def electron (js/require "electron"))
(def ipc (.-ipcRenderer electron))

;(defonce proc (js/require "child_process"))

;          js-args (clj->js (or args []))
;          p (.spawn proc cmd js-args)]
;      (.on p "error" (comp append-to-out
;                           #(str % "\n")))
;      (.on (.-stderr p) "data" append-to-out)
;      (.on (.-stdout p) "data" append-to-out))

(defn root-component []
  [:div#global-root.draggable-region
   [:input {:type "text"}]
   [:span (str "hi" @(rf/subscribe [:active-mode]))]])





;(js/document.addEventListener "keydown" #(when 
;                                           (= "Escape" (str (.-key %)))
;                                           (.send ipc "message" ":hide")))





(enable-re-frisk-remote! {:enable-re-frame-10x? true})

(defn dev-setup []
  #_(when config/debug?)
  (enable-console-print!)
  (println "we be cljs"))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render
    [root-component]
    (js/document.getElementById "app")))

(defn init-main-comms []
  (.on ipc "message" (fn [_ new-mode]
                       (println "msg->ui" new-mode)
                       (rf/dispatch [:set-active-mode (case new-mode
                                                        ":new-note" :new-note
                                                        ":search" :search)]))))

(defn ^:export init []
  (rf/dispatch-sync [:reset-db])
  (rf/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])
  (dev-setup)
  (mount-root)
  (init-main-comms))


(println ^:wassup {:this 'is_roto_
                   'a    "clojure map"})
