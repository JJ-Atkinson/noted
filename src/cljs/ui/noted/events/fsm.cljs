(ns noted.events.fsm
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as tmb]
            [noted.events.common-transitions :as ct]
            ))

; this will not be a complete implementation of a finite state machine. It
; will only govern inter-state transitions. anything that is constrained to
; the current page should still use the normal method of using reg-event-db.

; the general plan here is to take the original event map, which contains
; cofx and original db and the original event handler, and allow all of those to
; be seen by each transition. 
; general naming.

; machine {:state {:transition-name action}}
; cofx map is the map of possible injected data from reg-event-fx, 
; (typically containing the db and the event map under :db and :event)
; collected-fx is the name of things that will be returned to reg-event-fx
; a result of :fsm/failure will return an empty collected-fx and do nothing
; an action may be composed easily, or can also be used raw. the args will
; be just [cofx-map] in that case.


(defn compose
  "takes a list of actions and returns a fn to reduce the event over.
  each action args comes in the form [cofx collected-fx]. to modify the db
  only, you can use unwrap-db. otherwise use the whole fn. make sure to pass
  down data in your actions, since other fns may have already processed it.
  returning :fsm/failure at any point will kill the whole cascade, and will
  ultimately return the empty map to reg-event-fx.
  
  
  !! Warning: does not start the collected-fx with a :db key. If you use
  `unwrap-db` at the beginning of your chain, it will insert the key for you.
  be wary."
  [& actions]
  (fn fsm-compose [cofx-map]
    (let [result
          (reduce (fn [collected-fx action]
                    (if (not= :fsm/failure collected-fx)
                      (action cofx-map collected-fx)
                      :fsm/failure))
                  {} actions)]
      (if (= result :fsm/failure)
        (do (tmb/error "fsm event failure " actions)
            {})
        result))))

(defn unwrap-db
  "either part of compose or by itself. way to only see :db and update it.
  it invokes (f db) and will update the db accordingly. injects :db into 
  cofx map if it is missing."
  [f]
  (fn fsm-unwrap-db
    ([cofx collected-fx]
     (let [ret (if (contains? collected-fx :db)
                 (update collected-fx :db f)
                 (assoc collected-fx :db (f (:db cofx))))]
       (if (= (:db ret) :fsm/failure)
         :fsm/failure
         ret)))
    ([cofx]
     (fsm-unwrap-db cofx {:db (:db cofx)}))))


(defn state-transition
  "can be used by itself or as part of compose"
  [path new-state]
  (unwrap-db (fn fsm-state-trans [db] (assoc-in db path new-state))))


(defn ident-and-warn [cofx-map]
  (tmb/warn "got cofx map that cannot be transitioned from. may be an 
  un-implemented feature, or intentional" cofx-map)
  {})


(defn invoke-fms
  "take the current state from :db in cofx-map and invoke the transition.
  if the transition fails, no change is applied and we throw errors to the console"
  [machine transition cofx-map]
  (let [curr-state (get-in cofx-map [:db :ui-common :mode])
        transitions-map (get machine curr-state)
        action (get transitions-map transition ident-and-warn)
        res (action cofx-map)]
    (if (not= res :fsm/failure)
      res
      {})))

(defn fx-handler
  "take a machine and a transition name and put it through the fms. must have the 
  :db cofx installed in the event handler (default re-frame behavior)"
  [machine transition]
  (fn fsm-refx-handler [cofx _]
    (tmb/debug "invoking transition " transition)
    (invoke-fms machine transition cofx)))

