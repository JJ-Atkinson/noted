(ns noted.views.preview-note
  (:require
    [noted.events.events-utils :refer [e>]]
    [noted.subs.subs :refer [<s]]
    [re-com.core :as rc]
    [reagent.core :as r]
    [noted.utils.components :as comps]
    [re-frame.core :as rf]))

(defn preview-note []
  [:div.preview-note
   [:div.top-bar [:h2.title (<s [:preview-note/title])]
    [:div.buttons
     [:input.search
      {:type     "button"
       :value    "< Search"
       :on-click #(e> [:set-active-mode :search])}]
     [:input.edit
      {:type     "button"
       :value    "Edit >"
       :on-click #(e> [:preview-note/goto-editor])}]]]
   (into [:div.tags]
         (map (fn [t] [:a
                       {:on-click #(e> [:preview-note/search-for-tag t])}
                       (str "#" t)])
              (<s [:preview-note/tags])))
   [:div.content {:dangerouslySetInnerHTML
                  {:__html (<s [:preview-note/content])}}]])