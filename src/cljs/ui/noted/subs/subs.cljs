(ns noted.subs.subs
  (:require [re-frame.core :as rf]
            [noted.search-tools :as st]
            [taoensso.timbre :as tmb]
            [clojure.string :as str]
            [markdown.core :as md]))

(defn <s [query] @(rf/subscribe query))

(rf/reg-sub
  :active-mode
  (fn [db _]
    (get-in db [:ui-common :mode])))

(rf/reg-sub
  :search-view/query-string
  (fn [db _]
    (get-in db [:search-view :query])))

(rf/reg-sub
  :all-notes
  (fn [db _]
    (map-indexed (fn [idx x] (assoc x :id idx)) (get-in db [:ui-common :notes]))))

(rf/reg-sub
  :private/all-notes->js
  :<- [:all-notes]
  (fn [all-notes _]
    (clj->js all-notes)))

(rf/reg-sub
  :search-view/query-results
  :<- [:search-view/query-string]
  :<- [:all-notes]
  :<- [:private/all-notes->js]
  (fn [[query all-notes js-notes] _]
    (st/search query all-notes js-notes)))

(rf/reg-sub
  :new-note-view/form-title
  (fn [db _]
    (get-in db [:new-note-view :note-form :title])))

(rf/reg-sub
  :new-note-view/form-content
  (fn [db _]
    (get-in db [:new-note-view :note-form :content])))

(rf/reg-sub
  :new-note-view/form-tags
  (fn [db _]
    (get-in db [:new-note-view :note-form :tags])))



(rf/reg-sub
  :note-viewer/title
  (fn [db _]
    (get-in db [:ui-common :notes 
                (get-in db [:note-viewer :id]) :title])))

(rf/reg-sub
  :note-viewer/content
  (fn [db _]
    (md/md->html (get-in db [:ui-common :notes
                          (get-in db [:note-viewer :id]) :content]))))

(rf/reg-sub
  :note-viewer/tags
  (fn [db _]
    (get-in db [:ui-common :notes
                (get-in db [:note-viewer :id]) :tags])))
