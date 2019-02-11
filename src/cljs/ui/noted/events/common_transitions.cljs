(ns noted.events.common-transitions)

(defn hide-window [cofx collected-fx]
  (assoc collected-fx :hide-window nil))