(ns noted.subs.subs
  (:require [re-frame.core :as rf]
            [noted.search-tools :as st]
            [taoensso.timbre :as tmb]
            [clojure.string :as str]
            [markdown.core :as md]
            [noted.subs.sub-utils :as su]))

(defn <s [query] @(rf/subscribe query))

(su/basic-sub :active-mode [:ui-common :mode])
(su/basic-sub :search-view/query-string [:search-view :query])
(su/basic-sub :all-notes [:ui-common :notes])
(su/basic-sub :note-editor/form-title [:note-editor :note-form :title])
(su/basic-sub :note-editor/form-content [:note-editor :note-form :content])
(su/basic-sub :note-editor/form-tags [:note-editor :note-form :tags])

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
