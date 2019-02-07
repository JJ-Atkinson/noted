(ns noted.views.note-editor
  (:require
    [noted.events.events-utils :refer [e>]]
    [noted.subs.subs :refer [<s]]
    [re-com.core :as rc]
    [reagent.core :as r]
    [noted.utils.components :as comps]
    [re-frame.core :as rf]))



(defn note-editor []
  [:div.note-editor
   [comps/editor 
    :key    "e"
    :type :input
    :class "title"
    :default-value (rf/subscribe [:note-editor/form-title])
    :on-change #(e> [:note-editor/update-form-title %])
    :placeholder "Title"
    :autofocus true]
   [comps/editor 
    :key "c"
    :type :textarea
    :class "content"
    :default-value (rf/subscribe [:note-editor/form-content])
    :on-change #(e> [:note-editor/update-form-content %])
    :placeholder "content"]
   [:div.bottom-bar
    [comps/editor
     :key "t"
     :type :input
     :class "tags"
     :default-value (rf/subscribe [:note-editor/form-tags])
     :on-change #(e> [:note-editor/update-form-tags %])
     :placeholder "#tags"]
     [:input.submit
      {:type "button"
       :value "Submit"
       :on-click #(e> [:note-editor/submit-note-form])}]]])
