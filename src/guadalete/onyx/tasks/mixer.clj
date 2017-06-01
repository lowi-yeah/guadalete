(ns guadalete.onyx.tasks.mixer
    (:require
      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.schema.onyx :refer [KafkaInputTask]]
      [taoensso.timbre :as log]
      [onyx.peer.operation :refer [kw->fn]]
      [guadalete.onyx.tasks.util]
      [guadalete.utils.util :refer [pretty validate!]]
      [guadalete.config
       [onyx :refer [onyx-defaults]]
       [kafka :as kafka-config]
       [task :as task-config]
       ]))

(defn add [& signal-values]
      (reduce + signal-values))

(defn multiply [& signal-values] (reduce * signal-values))

(defn mix! [mixin-fn-key state segment]
      ;(let [funktion (kw->fn mixin-fn-key)
      ;      values (map #(get % :value) (vals state))
      ;      mix (if (not-empty values)
      ;            (apply funktion values)
      ;            :empty)
      ;      timestamps (->> state (vals) (map #(get % :at)))
      ;      at (if (not-empty timestamps)
      ;           (apply max timestamps)
      ;           :never)]
      ;     (if (not= mix :empty)
      ;       ; return a tupe with the current segment first
      ;       ; it will be filtered again in the flow condition
      ;       ; this is necessart, since otherwise the state won't update
      ;       [segment {:value mix :at at :mixed true}]
      ;       ; return the segment and NOT an empty vector, as otherwise the state will never get initialized
      ;       ; strange, but what is one to do?
      ;       [segment]))
      [segment]
      )

(defn inject-state
      [{:keys [onyx.core/windows-state onyx.core/params]} _lifecycle]
      (let [state (-> @windows-state (first) (get-in [:state 1]))]
           {:onyx.core/params (conj params state)}))

(defn inject-mixin-fn
      "Injects the mixin function (@see functions above)."
      [{:keys [onyx.core/task-map]} lifecycle]
      {:onyx.core/params [(:mixin-fn task-map)]})

(def lifecycle-calls
  {:lifecycle/before-batch      inject-state
   :lifecycle/before-task-start inject-mixin-fn})

(defn mixed? [event old {:keys [mixed]} all-new]
      (not (nil? mixed)))

(s/defn signal-mixer
        [task-name :- s/Keyword
         mixin-fn-name :- s/Str]
        (let [mixin-fn (keyword "guadalete.onyx.tasks.mixer" mixin-fn-name)
              window-id (keyword (name task-name) "window")
              task-map (merge
                         (onyx-defaults)
                         {:onyx/name           task-name
                          :onyx/fn             ::mix!
                          :onyx/type           :function
                          :onyx/uniqueness-key :at
                          :onyx/doc            "Keeps track of incoming signals and their values and triggers the emission of merged maps."
                          :mixin-fn            mixin-fn})
              window {:window/id          window-id
                      :window/task        task-name
                      :window/type        :global
                      :window/aggregation :guadalete.onyx.windowing/map-latest
                      :window/window-key  :at
                      :map-key            :id}
              lifecycles [{:lifecycle/task  task-name
                           :lifecycle/calls ::lifecycle-calls}]
              ;trigger {:trigger/window-id         window-id
              ;         :trigger/refinement        :onyx.refinements/accumulating
              ;         :trigger/on                :onyx.triggers/segment
              ;         :trigger/fire-all-extents? true
              ;         :trigger/threshold         [1 :element]
              ;         :trigger/sync              ::dump-window!}

              flow-conditions [{:flow/from           task-name
                                :flow/to             :all
                                :flow/predicate      ::mixed?
                                :flow/exclude-keys   [:mixed]
                                :flow/short-circuit? true
                                :flow/doc            "Emits segment iff it is a mixed signal"}]


              task {:task   {:task-map        task-map
                             :windows         [window]
                             :triggers        []
                             :lifecycles      lifecycles
                             :flow-conditions flow-conditions}
                    :schema {:task-map        os/TaskMap
                             :windows         [os/Window]
                             :triggers        [os/Trigger]
                             :lifecycles      [os/Lifecycle]
                             :flow-conditions [os/FlowCondition]}}]
             task))
