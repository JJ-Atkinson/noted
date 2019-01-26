(ns electro-note.subs.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  :active-mode
  (fn [db _]
    (get-in db [:common-data :mode])))