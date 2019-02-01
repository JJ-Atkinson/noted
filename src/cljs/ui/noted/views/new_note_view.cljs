(ns noted.views.new-note-view
  (:require
    [noted.events.events-utils :refer [e>]]
    [noted.subs.subs :refer [<s]]
    [re-com.core :as rc]
    [reagent.core :as r]
    [noted.utils.components :as comps]
    [re-frame.core :as rf]))



(defn new-note-view []
  [:div.new-note-view
   [comps/editor 
    :key    "e"
    :type :input
    :class "title"
    :default-value (rf/subscribe [:new-note-view/form-title])
    :on-change #(e> [:new-note-view/update-form-title %])
    :placeholder "Title"]
   [comps/editor 
    :key "c"
    :type :textarea
    :class "content"
    :default-value (rf/subscribe [:new-note-view/form-content])
    :on-change #(e> [:new-note-view/update-form-content %])
    :placeholder "content"]
   [:div.bottom-bar
    [comps/editor
     :key "t"
     :type :input
     :class "tags"
     :default-value (rf/subscribe [:new-note-view/form-tags])
     :on-change #(e> [:new-note-view/update-form-tags %])
     :placeholder "#tags"]
     [:input.submit
      {:type "button"
       :value "Submit"
       :on-click #(e> [:new-note-view/submit-note-form])}]]])
