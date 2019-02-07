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
    (get-in db [:ui-common :notes])))

(rf/reg-sub
  :private/all-notes->js
  :<- [:all-notes]
  (fn [all-notes _]
    ; simplifies life in the search function
    (clj->js (vals all-notes))))

(rf/reg-sub
  :search-view/query-results
  :<- [:search-view/query-string]
  :<- [:all-notes]
  :<- [:private/all-notes->js]
  (fn [[query all-notes js-notes] _]
    (vals (st/search query all-notes js-notes))))

(rf/reg-sub
  :note-editor/form-title
  (fn [db _]
    (get-in db [:note-editor :note-form :title])))

(rf/reg-sub
  :note-editor/form-content
  (fn [db _]
    (get-in db [:note-editor :note-form :content])))

(rf/reg-sub
  :note-editor/form-tags
  (fn [db _]
    (get-in db [:note-editor :note-form :tags])))



(rf/reg-sub
  :preview-note/title
  (fn [db _]
    (get-in db [:ui-common :notes 
                (get-in db [:preview-note :id]) :title])))

(rf/reg-sub
  :preview-note/content
  (fn [db _]
    (md/md->html (get-in db [:ui-common :notes
                          (get-in db [:preview-note :id]) :content]))))

(rf/reg-sub
  :preview-note/tags
  (fn [db _]
    (get-in db [:ui-common :notes
                (get-in db [:preview-note :id]) :tags])))
