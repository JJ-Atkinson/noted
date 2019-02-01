(ns noted.utils.components
  (:require [reagent.core :as r]
            [cljs.spec.alpha :as s]
            [noted.utils.common :as uc]))

(s/def ::class string?)
(s/def ::on-change fn?)
(s/def ::default-value #(or (string? %) true #_"need atom?"))
(s/def ::type #{:textarea :input})
(s/def ::on-change-time #{:continuous :on-action})
(s/def ::placeholder string?)
(s/def ::key string?)

(s/def ::editor-opts (s/keys :req-un [::class
                                      ::on-change
                                      ::default-value
                                      ::type
                                      ::on-change-time
                                      ::key]))

(def default-editor-opts
  {:class ""
   :default-value ""
   :type :input
   :on-change-time :on-action
   :placeholder ""
   :key ""})

(defn editor [& opts]
  ":class :default-value :type :on-change-time :placeholder :key :on-change"
  (let [{:keys [class on-change default-value type placeholder key]} 
              (merge default-editor-opts
                     (uc/parse-opts opts default-editor-opts ::editor-opts))]
    (r/create-class
      {:display-name   "dynamic-text-area"
       :reagent-render (fn [] [type
                               {:class         class
                                :on-change     #(on-change (.-value (.-target %)))
                                :value (if (string? default-value)
                                                 default-value
                                                 @default-value)
                                :placeholder   placeholder}])})))
