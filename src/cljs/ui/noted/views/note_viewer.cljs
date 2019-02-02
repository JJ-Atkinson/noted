(ns noted.views.note-viewer
  (:require
    [noted.events.events-utils :refer [e>]]
    [noted.subs.subs :refer [<s]]
    [re-com.core :as rc]
    [reagent.core :as r]
    [noted.utils.components :as comps]
    [re-frame.core :as rf]))

(defn note-viewer []
  [:div.note-viewer
   [:div.top-bar [:h2.title (<s [:note-viewer/title])]
    [:div.buttons
     [:input.search
      {:type     "button"
       :value    "< Search"
       :on-click #(e> [:set-active-mode :search])}]
     [:input.edit
      {:type     "button"
       :value    "Edit >"
       :on-click #(e> [:note-viewer/goto-editor])}]]]
   (into [:div.tags]
         (map (fn [t] [:a
                       {:on-click #(e> [:note-viewer/search-for-tag t])}
                       (str "#" t)])
              (<s [:note-viewer/tags])))
   [:div.content {:dangerouslySetInnerHTML
                  {:__html (<s [:note-viewer/content])}}]])