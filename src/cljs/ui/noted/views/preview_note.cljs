(ns noted.views.preview-note
  (:require
    [noted.events.events-utils :refer [e>]]
    [noted.subs.subs :refer [<s]]
    [re-com.core :as rc]
    [reagent.core :as r]
    [noted.utils.components :as comps]
    [re-frame.core :as rf]))

(defn preview-note []
  (let [not-pinned? (not (<s [:preview-note/pinned-mode?]))]
    [:div.preview-note
     [:div.top-bar
      [:div.left
       [:h2.title (<s [:preview-note/title])]
       [:span.hint (<s [:preview-note/see-insertion-tag])]]
      [:div.buttons
       (when not-pinned?
         [:input.search
          {:type     "button"
           :value    "< Search"
           :on-click #(e> [:search-view/goto-view])}])
       [:i.pin.material-icons
        {:type     "button"
         :on-click #(e> [:preview-note/pin])}
        (if not-pinned?
          "lock_open"
          "close")]
       (when not-pinned?
         [:input.edit
          {:type     "button"
           :value    "Edit >"
           :on-click #(e> [:preview-note/goto-editor])}])]]
     (into [:div.tags]
           (map (fn [t] [:a
                         {:on-click #(e> [:search-view/goto-view-with-search t])}
                         (str "#" t)])
                (<s [:preview-note/tags])))
     [:div.content {:dangerouslySetInnerHTML
                    {:__html (<s [:preview-note/content])}}]]))