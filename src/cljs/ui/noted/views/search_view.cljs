(ns noted.views.search-view
  (:require
    [noted.events.events-utils :refer [e>]]
    [noted.subs.subs :refer [<s]]
    [re-com.core :as rc]
    [noted.utils.components :as comps]
    [re-frame.core :as rf]
    [taoensso.timbre :as tmb]))


(defn render-tags [tags]
  [:div.tags (map (fn [tag]
                    [:a
                     {:on-click (fn [e]
                                  (tmb/debug e)
                                  (.stopPropagation e)
                                  (e> [:search-view/insert-tag-into-search tag])
                                  true)
                      :key tag}
                     (str "#" tag)])
                  tags)])

(defn search-view []
  [:div.search-view
   [comps/editor
    :autofocus true
    :key "e"
    :on-change #(e> [:search-view/update-search-query %])
    :default-value (rf/subscribe [:search-view/query-string])
    :class "search"
    :placeholder "#tag fuzzy search"]
   [
    
    
    :div.results (map (fn [note]
                        [:div
                         {:on-click #(e> [:preview-note/set-and-open-id (:id note)])
                          :key      (str (:id note))}
                         [:span.title (:title note)]
                         [:p.content
                          {:dangerouslySetInnerHTML
                           {:__html (:content note)}}]
                         [render-tags (:tags note)]])
                      (<s [:search-view/query-results]))]])