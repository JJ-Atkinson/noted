(ns noted.events.common-transitions)

(defn hide-window [cofx collected-fx]
  (assoc collected-fx :hide-window nil))

(defn close-window [cofx collected-fx]
  (assoc collected-fx :close-window nil))

(defn open-new-window [cofx collected-fx]
  (assoc collected-fx :open-new-window nil))