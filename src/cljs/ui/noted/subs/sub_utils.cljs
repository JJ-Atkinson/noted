(ns noted.subs.sub-utils
  (:require [re-frame.core :as rf]))

(defn basic-sub [name path]
  (rf/reg-sub
    name
    (fn [db _]
      (get-in db path))))