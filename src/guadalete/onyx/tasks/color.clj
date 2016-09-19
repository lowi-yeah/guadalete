(ns guadalete.onyx.tasks.color
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


(defn compose! [channel state segment]
      (log/debug "compose!" channel segment state)
      (assoc segment :channel channel))

(defn inject-state
      [{:keys [onyx.core/windows-state onyx.core/params]} _lifecycle]
      (let [state (-> @windows-state (first) (get-in [:state 1]))]
           {:onyx.core/params (conj params state)}))

(defn inject-channel
      "Injects the color channel this signal controlls"
      [{:keys [onyx.core/task-map]} lifecycle]
      {:onyx.core/params [(:color/channel task-map)]})

(def lifecycle-calls
  {:lifecycle/before-batch      inject-state
   :lifecycle/before-task-start inject-channel})

(defn mixed? [event old {:keys [mixed]} all-new]
      (not (nil? mixed)))

(defn color-aggregation-fn-init [window]
      {:h 0 :s 0 :v 0})

(defn color-aggregation-fn [window state segment]
      (log/debug "color-aggregation-fn | segment" segment)
      segment)

(defn color-super-aggregation [window state-1 state-2]
      (into state-1 state-2))

(defn color-aggregation-apply-log [window state v]
      (let [{:keys [id data at mixed] :as segment} v]
           (clojure.core/assoc state (keyword id) {:value data :at at})))

(def color-aggregation
  {:aggregation/init                 color-aggregation-fn-init
   :aggregation/create-state-update  color-aggregation-fn
   :aggregation/apply-state-update   color-aggregation-apply-log
   :aggregation/super-aggregation-fn color-super-aggregation})


(s/defn color
        [task-name :- s/Keyword
         channel :- s/Keyword]
        (let [window-id (keyword (name task-name) (str channel "-window"))
              task-map (merge
                         (onyx-defaults)
                         {:onyx/name           task-name
                          :onyx/fn             ::compose!
                          :onyx/type           :function
                          :onyx/uniqueness-key :at
                          :onyx/doc            "Keeps track of incoming signals on the input channels of the color and emmits a combined color-map"
                          :color/channel       channel})
              window {:window/id          window-id
                      :window/task        task-name
                      :window/type        :global
                      :window/aggregation ::color-aggregation
                      :window/window-key  :at
                      :map-key            :id}
              lifecycles [{:lifecycle/task  task-name
                           :lifecycle/calls ::lifecycle-calls}]

              flow-conditions [{:flow/from           task-name
                                :flow/to             :all
                                :flow/predicate      ::mixed?
                                :flow/exclude-keys   [:mixed]
                                :flow/short-circuit? true
                                :flow/doc            "Emits segment iff it is a mixed signal"}]

              task {:task   {:task-map        task-map
                             :windows         [window]
                             :lifecycles      lifecycles
                             :flow-conditions flow-conditions}
                    :schema {:task-map        os/TaskMap
                             :windows         [os/Window]
                             :lifecycles      [os/Lifecycle]
                             :flow-conditions [os/FlowCondition]}}]
             task))
