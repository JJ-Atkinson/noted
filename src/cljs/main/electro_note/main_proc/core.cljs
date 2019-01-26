(ns electro-note.main-proc.core
  (:require [taoensso.timbre :as tmb]))

(def electron (js/require "electron"))
(def app (.-app electron))
(def browser-window (.-BrowserWindow electron))
(def crash-reporter (.-crashReporter electron))
(def global-shortcut (.-globalShortcut electron))
(def ipc (.-ipcMain electron))

(def main-window (atom nil))

(def last-opened-mode (atom :search))




(defn send-mode-switch-message [new-mode]
  (when @main-window
    (let [contents (.-webContents ^js/electron.BrowserWindow @main-window)]
      (.send contents "message" (str new-mode)))))

(defn toggle-window [new-mode]
  (if-let [win ^js/electron.BrowserWindow @main-window]
    (let [mode-change? (not= new-mode @last-opened-mode)
          visible? (.isVisible ^js/electron.BrowserWindow @main-window)
          open? (or mode-change? (not visible?))]
      (when mode-change?
        (send-mode-switch-message new-mode)
        (reset! last-opened-mode new-mode))
      (if open?
        (.show win)
        (.hide win)))))

(defn hide-window []
  (if-let [win ^js/electron.BrowserWindow @main-window]
    (.hide win)))


(defn init-browser []
  (reset! main-window (browser-window.
                        (clj->js {:with      200
                                  :height    500
                                  ;:transparent false
                                  :show      true
                                  :resizable true
                                  :frame     true
                                  ;:skipTaskbar true
                                  })))
  ; Path is relative to the compiled js file (main.js in our case)
  (.loadURL ^js/electron.BrowserWindow @main-window (str "file://" js/__dirname "/public/index.html"))
  #_(.on ^js/electron.BrowserWindow @main-window "closed" #(reset! main-window nil))
  #_(let [contents (. ^js/electron.BrowserWindow @main-window -webContents)]
      (.on contents "did-finish-load" #(.send contents "message" "Hello window!")))

  #_(.minimize ^js/electron.BrowserWindow @main-window)
  (.register global-shortcut "CommandOrControl+Shift+S" #(toggle-window :search))
  (.register global-shortcut "CommandOrControl+Shift+N" #(toggle-window :new-note)))

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