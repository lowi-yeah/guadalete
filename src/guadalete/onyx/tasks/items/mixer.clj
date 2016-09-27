(ns guadalete.onyx.tasks.items.mixer
    (:require
      [taoensso.timbre :as log]
      [schema.core :as s]
      [onyx.schema :as os]
      [clojurewerkz.statistiker.statistics :as statistics]
      [guadalete.utils.util :as util]
      [onyx.peer.operation :refer [kw->fn]]
      [guadalete.config.onyx :refer [onyx-defaults]]
      [guadalete.onyx.tasks.identity :refer [identity-task log-task dissoc-task dissoc-and-log-task]]))


;//   _                   _
;//  (_)_ _  __ ___ _ __ (_)_ _  __ _
;//  | | ' \/ _/ _ \ '  \| | ' \/ _` |
;//  |_|_||_\__\___/_|_|_|_|_||_\__, |
;//                             |___/

(defn inject-channel
      "Injects the mixer channel as a parameter into ::assoc-channel."
      [{:keys [onyx.core/task-map]} lifecycle]
      {:onyx.core/params [(:mixer/channel task-map)]})

(def in-lifecycle-calls
  {:lifecycle/before-task-start inject-channel})

(defn assoc-channel [channel segment]
      (assoc segment :channel channel))

(s/defn in
        [{:keys [name channel] :as attributes}]
        (let [task-map (merge
                         (onyx-defaults)
                         {:onyx/name           name
                          :onyx/fn             ::assoc-channel
                          :onyx/type           :function
                          :onyx/uniqueness-key :at
                          :onyx/doc            "Assocs the color channel with each segment."
                          :mixer/channel       channel})
              lifecycles [{:lifecycle/task  name
                           :lifecycle/calls ::in-lifecycle-calls}]]
             {:task   {:task-map   task-map
                       :lifecycles lifecycles}
              :schema {:task-map   os/TaskMap
                       :lifecycles [os/Lifecycle]}}))

;//   _         _    _
;//  (_)_ _  __(_)__| |___
;//  | | ' \(_-< / _` / -_)
;//  |_|_||_/__/_\__,_\___|
;//

;mixer/in! {:data 0.62, :id :9eca887a-8303-4cee-8bd6-a25bb9c11bf0-out, :at 1474981389373, :channel 0}

;; WINDOW
;; ————————————————————
(defn signal-aggregation-fn-init [window] {})
(defn signal-aggregation-fn [window state segment] segment)
(defn signal-super-aggregation [window state-1 state-2] (into state-1 state-2))
(defn signal-aggregation-apply-log [window state v]
      (let [{:keys [channel data at] :as segment} v]
           (clojure.core/assoc state channel v)))

(def aggregate-signals
  {:aggregation/init                 signal-aggregation-fn-init
   :aggregation/create-state-update  signal-aggregation-fn
   :aggregation/apply-state-update   signal-aggregation-apply-log
   :aggregation/super-aggregation-fn signal-super-aggregation})

;; LIFECYCLE
;; ————————————————————
(defn inject-state
      [{:keys [onyx.core/windows-state onyx.core/params]} _lifecycle]
      (let [state (-> @windows-state (first) (get-in [:state 1]))]
           {:onyx.core/params (conj params state)}))

(defn inject-mixin-fn
      "Injects the mixin function (@see functions above)."
      [{:keys [onyx.core/task-map]} lifecycle]
      {:onyx.core/params [(:mixer/fn task-map)]})

(def inner-lifecycle-calls
  {:lifecycle/before-batch      inject-state
   :lifecycle/before-task-start inject-mixin-fn})

;; FUNCTION
;; ————————————————————
(defn mix* [mixin-fn-key state segment]
      (let [funktion (kw->fn mixin-fn-key)
            values (->> (vals state)
                        (map #(get % :data))
                        (into []))
            mixed (if (not-empty values)
                    (apply funktion values)
                    0)]
           (assoc segment :combined mixed)))

;; TASK
;; ————————————————————
(s/defn mix!
        [{:keys [name mixin-fn] :as attributes}]
        (let [window-id (keyword (str "w-" (util/uuid)))
              task-map (merge
                         (onyx-defaults)
                         {:onyx/name           name
                          :onyx/fn             ::mix*
                          :onyx/type           :function
                          :onyx/uniqueness-key :at
                          :onyx/doc            "Combines two signals."
                          :mixer/fn            mixin-fn})
              windows [{:window/id          window-id
                        :window/task        name
                        :window/type        :global
                        :window/aggregation ::aggregate-signals
                        :window/window-key  :at
                        :map-key            :id}]
              lifecycles [{:lifecycle/task  name
                           :lifecycle/calls ::inner-lifecycle-calls}]]
             {:task   {:task-map   task-map
                       :windows    windows
                       :lifecycles lifecycles}
              :schema {:task-map   os/TaskMap
                       :windows    [os/Window]
                       :lifecycles [os/Lifecycle]}}))
;//            _
;//   ___ _  _| |_
;//  / _ \ || |  _|
;//  \___/\_,_|\__|
;//
(defn out* [segment]
      (let [combined (:combined segment)]
           (-> segment
               (assoc :data combined)
               (dissoc :channel :combined :id))))

(s/defn out
        [{:keys [name channel] :as attributes}]
        (let [task-map (merge
                         (onyx-defaults)
                         {:onyx/name           name
                          :onyx/fn             ::out*
                          :onyx/type           :function
                          :onyx/uniqueness-key :at
                          :onyx/doc            "Transforms mixed segments."
                          :mixer/channel       channel})]
             {:task   {:task-map task-map}
              :schema {:task-map os/TaskMap}}))

;color/in! :brightness {:data 0.62, :id :9eca887a-8303-4cee-8bd6-a25bb9c11bf0-out, :at 1474981856301}
;//         _     _         __              _   _
;//   _ __ (_)_ ___)_ _    / _|_  _ _ _  __| |_(_)___ _ _  ___
;//  | '  \| \ \ / | ' \  |  _| || | ' \/ _|  _| / _ \ ' \(_-<
;//  |_|_|_|_/_\_\_|_||_| |_|  \_,_|_||_\__|\__|_\___/_||_/__/
;//
(s/defn avg
        "Mixin function which averages the incoming signals"
        [& numbers]
        (statistics/mean numbers))

(s/defn product
        "Mixin function which averages the incoming signals"
        [& numbers]
        ;(log/debug "mix/avg" numbers (statistics/mean numbers))
        (statistics/product numbers))