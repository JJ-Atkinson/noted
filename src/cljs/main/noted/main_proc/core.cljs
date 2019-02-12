(ns noted.main-proc.core
  (:require [taoensso.timbre :as tmb]
            [clojure.string :as str]))

(def electron (js/require "electron"))
(def fs (js/require "fs"))
(def app (.-app electron))
(def browser-window (.-BrowserWindow electron))
(def crash-reporter (.-crashReporter electron))
(def global-shortcut (.-globalShortcut electron))
(def ipc (.-ipcMain electron))

(def main-window (atom nil))



(def settings-file (str (.getPath app "documents") "/noted-settings.edn"))


(defn get-storage-file-loc []
  (if (.existsSync fs settings-file)
    (:storage (cljs.reader/read-string (str (.readFileSync fs settings-file))))
    (str (.getPath app "documents") "/noted-store.edn\"")))

(def storage-file (get-storage-file-loc))
(def storage-file-old (str storage-file ".old"))


(defn get-stored-notes []
  (if (.existsSync fs storage-file)
    (str (.readFileSync fs storage-file))
    "{}"))

(defn dispatch-stored-notes []
  (if-let [win ^js/electron.BrowserWindow @main-window]
    (.send (.-webContents win)
           "message"
           (str "{:store " (get-stored-notes) "}"))))


(defn store-notes [notes-str]
  (when (.existsSync fs storage-file)
    (.renameSync fs storage-file storage-file-old))
  (.writeFileSync fs storage-file notes-str))


(defn window-visible? []
  (if-let [win ^js/electron.BrowserWindow @main-window]
    (.isVisible win)
    false))


(defn send-mode-switch-message [mode visible?]
  (if-let [win ^js/electron.BrowserWindow @main-window]
    (.send (.-webContents win)
           "message"
           (str {:mode     mode
                 :visible? visible?}))))


(defn show-window
  ([mode]
   (if-let [win ^js/electron.BrowserWindow @main-window]
     (let [vis? (window-visible?)]
       (.show win)
       (send-mode-switch-message mode vis?))))
  ([]
   (if-let [win ^js/electron.BrowserWindow @main-window]
     (.show win))))


(defn hide-window []
  (if-let [win ^js/electron.BrowserWindow @main-window]
    (.hide win)))


(defn make-window []
  (reset! main-window (browser-window.
                        (clj->js {:with      200
                                  :height    500
                                  :show      false
                                  :resizable true
                                  :frame     false
                                  })))

  ; Path is relative to the compiled js file (main.js in our case)
  (.loadURL ^js/electron.BrowserWindow @main-window (str "file://" js/__dirname "/public/index.html"))
  #_(.on ^js/electron.BrowserWindow @main-window "closed" #(reset! main-window nil))
  (let [contents (.-webContents ^js/electron.BrowserWindow @main-window)]
    (.on contents "did-finish-load" #(show-window)))
  #_(.minimize ^js/electron.BrowserWindow @main-window))

(defn create-keymap []
  (.register global-shortcut "CommandOrControl+Shift+N" #(show-window :note-editor))
  (.register global-shortcut "CommandOrControl+Shift+S" #(show-window :search-view)))

; CrashReporter can just be omitted
(.start crash-reporter
        (clj->js
          {:companyName "MyAwesomeCompany"
           :productName "MyAwesomeApp"
           :submitURL   "https://example.com/submit-url"
           :autoSubmit  false}))

#_(.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
(.on app "ready" #(do (make-window)
                      (create-keymap)))

(.on app "will-quit" #(.unregisterAll global-shortcut))

(.on ipc "message" (fn [_ e] (do (tmb/debug "msg->main" e)
                                 (cond
                                   (= e ":hide")
                                   (hide-window)

                                   (str/starts-with? e ":store")
                                   (store-notes (apply str (drop 6 e)))

                                   (= e ":pull")
                                   (dispatch-stored-notes)

                                   (= e ":create-new")
                                   (make-window)

                                   :else (tmb/debug "forgot ipc message? " e)))))