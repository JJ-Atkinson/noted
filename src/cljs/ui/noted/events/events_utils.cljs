(ns noted.events.events-utils
  (:require [re-frame.core :as rf]
            [noted.specs :as specs]))


(def e> rf/dispatch)

(def default-interceptors [specs/check-spec-interceptor
                           rf/trim-v])

