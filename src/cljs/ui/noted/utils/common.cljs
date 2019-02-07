(ns noted.utils.common
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [taoensso.timbre :as tmb]))

(defn any-?
  ^{:doc "Returns true if (pred x) is logical true for any x in coll,
  else false."}
  ([pred xs]
   (loop [nxs xs]
     (cond
       (pred (first nxs)) true
       (empty? nxs) false
       :else (recur (rest nxs)))))
  ([xs] (any-? some? xs)))

(defn parse-opts
  ([opts]
   (when (not (even? (count opts)))
     (throw (ex-cause (str "incorrect arity on optional args: " opts))))
   (into {} (map vec (partition 2 opts))))
  
  ([opts defaults spec]
    (let [res (merge defaults (parse-opts opts))]
      (if (s/valid? spec res)
        res
        (do
          (tmb/error "bad opts" res)
          [:noted.utils.common/invalid-opts "check the opts"])))))


(defn process-tag-str [s]
  (remove empty? (-> s
                     (str/lower-case)
                     (str/replace "#" "")
                     (str/split " "))))

(defn stringify-tags [tgs]
  (str/join " " (map (partial str "#") tgs)))
