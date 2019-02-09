(ns noted.events.fsm
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as tmb]))

; this will not be a complete implementation of a finite state machine. It
; will only govern inter-state transitions. anything that is constrained to
; the current page should still use the normal method of using reg-event-db.

; the general plan here is to take the original event map, which contains
; cofx and original db and the original event handler, and allow all of those to
; be seen by each transition. 
; general naming.

; machine {:state {:transition-name action}}
; cofx map is the map of possible injected data from reg-event-fx
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
                      collected-fx))
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
     (if (contains? collected-fx :db)
       (update collected-fx :db f)
       (assoc collected-fx :db (f (:db cofx)))))
    ([cofx]
     {:db (f (:db cofx))})))


(defn state-transition
  "can be used by itself or as part of compose"
  [new-state]
  (unwrap-db (fn fsm-state-trans [db]
               (tmb/spy (assoc-in db [:ui-common :mode] new-state)))))


(def machine
  {:preview-note {:hide         (compose
                                  (state-transition :hidden)
                                  ;(hide-fx)
                                  )
                  :esc          (compose
                                  (state-transition :hidden)
                                  ;(hide-fx)
                                  )
                  :search-view  (state-transition :search-view)
                  :note-editor  (state-transition :note-editor)
                  :search-tag   (compose
                                  (state-transition :search-view)
                                  ;(change-tag-search)
                                  )
                  :edit-preview (compose
                                  (state-transition :note-editor)
                                  ;(copy-preview-note-form)
                                  )
                  :pin          (compose
                                  (state-transition :pinned)
                                  ;(dispatch-main open-new)
                                  )}
   :search-view  {:note-editor (state-transition :note-editor)
                  :view-note   (compose
                                 (state-transition :preview-note)
                                 ;(move-search-view-to-preview)
                                 )
                  :hide        (compose
                                 (state-transition :hidden)
                                 ;(hide-fx)
                                 )
                  :esc         (compose
                                 (state-transition :hidden)
                                 ;(clear-search-query)
                                 )}
   :note-editor  {:esc         (compose
                                 (state-transition :hidden)
                                 ;(clear-editor)
                                 )
                  :submit      (compose
                                 (state-transition :hidden)
                                 ;(clear-editor)
                                 ;(submit-note)
                                 ;(hide-fx)
                                 )
                  :search-view (state-transition :search-view)}
   :hidden       {:search-view  (state-transition :search-view)
                  :note-editor  (state-transition :note-editor)}
   :pinned       {:esc (dispose-window)}
   })


(defn invoke-fms
  "take the current state from :db in cofx-map and invoke the transition.
  if the transition fails, no change is applied and we throw errors to the console"
  [transition cofx-map]
  (let [curr-state (get-in cofx-map [:db :mode])
        transitions-map (get machine curr-state)
        action (get transitions-map transition)
        res (action cofx-map)]
    (if (not= res :fsm/failure)
      res
      {})))

(defn fx-handler
  "take a transition name and put it through the fms. must have the 
  :db cofx installed in the event handler"
  [transition]
  (fn fsm-refx-handler [cofx _]
    (invoke-fms transition cofx)))

;(rf/reg-event-fx
;  :note-editor/submit-note-form
;  ;eu/default-interceptors
;  (fx-handler :esc))