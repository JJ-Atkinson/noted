(ns noted.main-proc.core
  (:require [taoensso.timbre :as tmb]))

(def electron (js/require "electron"))
(def app (.-app electron))
(def browser-window (.-BrowserWindow electron))
(def crash-reporter (.-crashReporter electron))
(def global-shortcut (.-globalShortcut electron))
(def ipc (.-ipcMain electron))

(def main-window (atom nil))

(def last-opened-mode (atom :search))



(defn window-visible? []
  (if-let [win ^js/electron.BrowserWindow @main-window]
    (.isVisible win)
    false))

(defn send-mode-switch-message [mode]
  (if-let [win ^js/electron.BrowserWindow @main-window]
    (.send (.-webContents win) 
           "message" 
           (str {:mode mode
                 :visible? (window-visible?)}))))

(defn show-window [mode]
  (if-let [win ^js/electron.BrowserWindow @main-window]
    (do (.show win)
        (send-mode-switch-message mode))))

(defn hide-window []
  (if-let [win ^js/electron.BrowserWindow @main-window]
    (.hide win)))



(defn init-browser []
  (reset! main-window (browser-window.
                        (clj->js {:with      200
                                  :height    500
                                  :show      true
                                  :resizable true
                                  :frame     false
                                  })))
  ; Path is relative to the compiled js file (main.js in our case)
  (.loadURL ^js/electron.BrowserWindow @main-window (str "file://" js/__dirname "/public/index.html"))
  #_(.on ^js/electron.BrowserWindow @main-window "closed" #(reset! main-window nil))
  #_(let [contents (. ^js/electron.BrowserWindow @main-window -webContents)]
      (.on contents "did-finish-load" #(.send contents "message" "Hello window!")))

  #_(.minimize ^js/electron.BrowserWindow @main-window)
  (.register global-shortcut "CommandOrControl+Shift+S" #(show-window :search))
  (.register global-shortcut "CommandOrControl+Shift+N" #(show-window :new-note)))

; CrashReporter can just be omitted
(.start crash-reporter
        (clj->js
          {:companyName "MyAwesomeCompany"
           :productName "MyAwesomeApp"
           :submitURL   "https://example.com/submit-url"
           :autoSubmit  false}))

#_(.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
(.on app "ready" init-browser)

(.on app "will-quit" #(.unregisterAll global-shortcut))

(.on ipc "message" (fn [_ e] (do (tmb/debug "msg->main" e)
                                 (case e
                                   ":hide" (hide-window)
                                   :default nil))))