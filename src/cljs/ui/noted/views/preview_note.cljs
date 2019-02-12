(ns noted.views.preview-note
  (:require
    [noted.events.events-utils :refer [e>]]
    [noted.subs.subs :refer [<s]]
    [re-com.core :as rc]
    [reagent.core :as r]
    [noted.utils.components :as comps]
    [re-frame.core :as rf]))

(defn button-bar []
  (let [pinned? (<s [:preview-note/pinned-mode?])
        deleting?   (<s [:preview-note/deleting?])
        search [:input.search
                {:type     "button"
                 :value    "< Search"
                 :on-click #(e> [:search-view/goto-view])}]
        pin [:i.pin.material-icons
             {:type     "button"
              :on-click #(e> [:preview-note/pin])}
             (if pinned?
               "close"
               "lock_open")]
        delete [:i.delete.material-icons
         {:type     "button"
          :on-click #(e> [:preview-note/delete-self])}
         "delete_outline"]
        confirm [:i.delete.material-icons
                 {:type     "button"
                  :on-click #(e> [:preview-note/delete-yes])}
                 "delete_forever"]
        cancel [:i.delete.material-icons
                {:type     "button"
                 :on-click #(e> [:preview-note/delete-no])}
                "arrow_back"]
        edit [:input.edit
              {:type     "button"
               :value    "Edit >"
               :on-click #(e> [:preview-note/goto-editor])}]]
    [:div.buttons
     (if pinned?
       (list pin)
       (if deleting?
         (list cancel confirm)
         (list search pin delete edit)))]))

(defn preview-note []
  
  [:div.preview-note
   [:div.top-bar
    [:div.left
     [:h2.title (<s [:preview-note/title])]
     [:span.hint (<s [:preview-note/see-insertion-tag])]]
    [button-bar]]
   (into [:div.tags]
         (map (fn [t] [:a
                       {:on-click #(e> [:search-view/goto-view-with-search t])}
                       (str "#" t)])
              (<s [:preview-note/tags])))
   [:div.content {:dangerouslySetInnerHTML
                  {:__html (<s [:preview-note/content])}}]])