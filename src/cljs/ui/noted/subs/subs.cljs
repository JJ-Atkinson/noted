(ns noted.subs.subs
  (:require [re-frame.core :as rf]
            [noted.search-tools :as st]
            [taoensso.timbre :as tmb]
            [clojure.string :as str]
            [markdown.core :as md]
            [noted.subs.sub-utils :as su]
            [noted.note-insertion]))

(defn <s [query] @(rf/subscribe query))

(su/basic-sub :active-mode [:ui-common :mode])
(su/basic-sub :search-view/query-string [:search-view :query])
(su/basic-sub :all-notes [:ui-common :notes])
(su/basic-sub :note-editor/form-title [:note-editor :note-form :title])
(su/basic-sub :note-editor/form-content [:note-editor :note-form :content])
(su/basic-sub :note-editor/form-tags [:note-editor :note-form :tags])
(su/basic-sub :preview-note/id [:preview-note :id])

(rf/reg-sub
  :all-notes-processed
  :<- [:all-notes]
  (fn [notes _]
    (noted.note-insertion/process-insertion notes)))


(rf/reg-sub
  :private/all-notes->js
  :<- [:all-notes-processed]
  (fn [all-notes _]
    ; simplifies life in the search function
    (clj->js  (vals all-notes))))

(rf/reg-sub
  :search-view/query-results
  :<- [:search-view/query-string]
  :<- [:all-notes-processed]
  :<- [:private/all-notes->js]
  (fn [[query all-notes-processed js-notes] _]
    (map su/render-contents (take 50 (vals (st/search query all-notes-processed js-notes))))))

(rf/reg-sub
  :preview-note/title
  (fn [db _]
    (get-in db [:ui-common :notes
                (get-in db [:preview-note :id]) :title])))

(rf/reg-sub
  :preview-note/content
  :<- [:all-notes-processed]
  :<- [:preview-note/id]
  (fn [[all-notes-processed id] _]
    (md/md->html (get-in all-notes-processed [id :content]))))

(rf/reg-sub
  :preview-note/see-insertion-tag
  :<- [:preview-note/id]
  (fn [id _] (noted.note-insertion/syntax-for id)))

(rf/reg-sub
  :preview-note/tags
  (fn [db _]
    (get-in db [:ui-common :notes
                (get-in db [:preview-note :id]) :tags])))

(rf/reg-sub
  :preview-note/pinned-mode?
  (fn [db _]
    (= :pinned (get-in db [:ui-common :mode]))))
