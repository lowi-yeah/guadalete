(ns guadalete.onyx.tasks.items.transition
    (:require
      [schema.core :as s]
      [taoensso.timbre :as log]
      [thi.ng.tweeny.core :as tween]
      [onyx.schema :as os]
      [onyx.peer.operation :refer [kw->fn]]
      [guadalete.utils.util :as util]
      [guadalete.config.onyx :refer [onyx-defaults]]
      [guadalete.onyx.tasks.identity :refer [identity-task log-task dissoc-task dissoc-and-log-task]]))


;//   _                   _
;//  (_)_ _  __ ___ _ __ (_)_ _  __ _
;//  | | ' \/ _/ _ \ '  \| | ' \/ _` |
;//  |_|_||_\__\___/_|_|_|_|_||_\__, |
;//                             |___/
(s/defn in
        [{:keys [name] :as attributes}]
        (log-task name "transition/in")
        ;(identity-task name)
        )

;//
;//   ___ __ _ ______
;//  / -_) _` (_-< -_)
;//  \___\__,_/__\___|
;//
(defn get-easing-fn [key]
      (condp = key
             :linear (tween/mix-linear)
             :smooth (tween/mix-cosine)
             :ease-in (tween/mix-exp 1.4)
             :ease-out (tween/mix-exp 0.4)))

;; WINDOW
;; ————————————————————
(defn ease-aggregation-fn-init [window] {})
(defn ease-aggregation-fn [window state segment] segment)
(defn ease-super-aggregation [window state-1 state-2] (into state-1 state-2))
(defn ease-aggregation-apply-log [_window _state segment]
      (:data segment))

(def ease-window-aggregate
  {:aggregation/init                 ease-aggregation-fn-init
   :aggregation/create-state-update  ease-aggregation-fn
   :aggregation/apply-state-update   ease-aggregation-apply-log
   :aggregation/super-aggregation-fn ease-super-aggregation})

;; LIFECYCLE
;; ————————————————————
(defn inject-state
      [{:keys [onyx.core/windows-state onyx.core/params]} _lifecycle]
      (let [state (-> @windows-state (first) (get-in [:state 1]))
            state (or state 0)]
           {:onyx.core/params (conj params state)}))

(defn inject-params
      "Injects the easing function (@see functions above)."
      [{:keys [onyx.core/task-map]} lifecycle]
      {:onyx.core/params [(:transition/easing task-map)
                          (:transition/duration task-map)
                          (:transition/delay task-map)
                          (:transition/resolution task-map)]})

(def ease-lifecycle-calls
  {:lifecycle/before-batch      inject-state
   :lifecycle/before-task-start inject-params})

;; FUNCTION
;; ————————————————————
(defn ease* [easing duration delay resolution previous segment]
      (let [num-slices (int (/ duration resolution))
            easing-fn (get-easing-fn easing)
            keyframes [[0 {:v {:data previous}
                           :f easing-fn}]
                       [(- num-slices 1) {:v {:data (:data segment)}}]]

            now (util/now)
            sliced-segments (->> (range num-slices)
                                 (mapv (fn [index]
                                           (-> index
                                               (tween/at keyframes)
                                               (assoc :at (+ now delay (* index resolution)))
                                               (assoc :id (:id segment))))))]
           sliced-segments))


(s/defn ease!
        [{:keys [name easing duration delay] :as attributes}]
        (let [window-id (keyword (str "w-" (util/uuid)))
              task-map (merge
                         (onyx-defaults)
                         {:onyx/name             name
                          :onyx/fn               ::ease*
                          :onyx/type             :function
                          :onyx/uniqueness-key   :at
                          :onyx/doc              "Tweens a signal value."
                          :transition/easing     easing
                          :transition/duration   duration
                          :transition/delay      delay
                          :transition/resolution 100})
              windows [{:window/id          window-id
                        :window/task        name
                        :window/type        :global
                        :window/aggregation ::ease-window-aggregate
                        :window/window-key  :at
                        :map-key            :id}]
              lifecycles [{:lifecycle/task  name
                           :lifecycle/calls ::ease-lifecycle-calls}]]
             {:task   {:task-map   task-map
                       :windows    windows
                       :lifecycles lifecycles}
              :schema {:task-map   os/TaskMap
                       :windows    [os/Window]
                       :lifecycles [os/Lifecycle]}})


        ;(log-task name "transition/ease!")
        )

;//      _     _
;//   __| |___| |__ _ _  _
;//  / _` / -_) / _` | || |
;//  \__,_\___|_\__,_|\_, |
;//                   |__/
(defn delay* [segment]
      (let [naptime (- (:at segment) (util/now))]
           (when (> naptime 0) (Thread/sleep naptime))
           segment))

(s/defn delay!
        [{:keys [name delay] :as attributes}]
        (let [task-map (merge
                         (onyx-defaults)
                         {:onyx/name           name
                          :onyx/fn             ::delay*
                          :onyx/type           :function
                          :onyx/uniqueness-key :at
                          :onyx/doc            "Delays a signal for some time."})]
             {:task   {:task-map task-map}
              :schema {:task-map os/TaskMap}}))


;//            _
;//   ___ _  _| |_
;//  / _ \ || |  _|
;//  \___/\_,_|\__|
;//

(s/defn out
        [{:keys [name] :as attributes}]
        (identity-task name))

;; FUNCTIONS
;; ————————————————————
;(defn delay* [mixin-fn-key state segment]
;      (let [funktion (kw->fn mixin-fn-key)
;            values (->> (vals state)
;                        (map #(get % :data))
;                        (into []))
;            mixed (if (not-empty values)
;                    (apply funktion values)
;                    0)]
;           (assoc segment :combined mixed)))
;
;(defn ease* [mixin-fn-key state segment]
;      (let [funktion (kw->fn mixin-fn-key)
;            values (->> (vals state)
;                        (map #(get % :data))
;                        (into []))
;            mixed (if (not-empty values)
;                    (apply funktion values)
;                    0)]
;           (assoc segment :combined mixed)))

