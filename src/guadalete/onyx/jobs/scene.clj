(ns guadalete.onyx.jobs.scene
    (:require
      [ubergraph.core :as uber]
      [ubergraph.alg :as alg]
      [taoensso.timbre :as log]

      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.onyx.tasks.scene :refer [node-task]]
      [guadalete.onyx.jobs.util :refer [empty-job add-task add-tasks add-flow-conditions]]

      [guadalete.utils.util :refer [pretty validate!]]))


(s/defn make-task
        [graph
         node-id :- s/Keyword]
        (let [{:keys [task-type] :as attrs} (uber/attrs graph node-id)]
             (node-task task-type node-id attrs)))


(s/defn make-flow-condition :- [os/FlowCondition]
        [graph edge]
        (let [attrs (uber/attrs graph edge)
              filter-fn (:gdlt/flow-filter attrs)]
             (when filter-fn
                   [{:flow/from      (:src edge)
                     :flow/to        [(:dest edge)]
                     :flow/predicate filter-fn}])))

(s/defn build-catalog*
        ;(s/defn ^:always-validate build-catalog*
        "Recursive helper for creating the task catalog of a scene-graph-job."
        [graph
         nodes :- [s/Keyword]
         flows :- os/Workflow
         catalog]
        (if (empty? nodes)
          catalog
          (let [[head & tail] nodes
                task (make-task graph head)
                catalog* (conj catalog task)
                ]
               (build-catalog* graph tail flows catalog*))))

(s/defn build-flow-conditions*
        ;(s/defn ^:always-validate build-catalog*
        "Recursive helper for creating the flow conditions between tasks."
        [graph
         edges :- [s/Keyword]
         result]
        (if (empty? edges)
          (->> result flatten (filter #(-> % nil? not)))
          (let [[head & tail] edges
                flow-condition (make-flow-condition graph head)
                result* (conj result flow-condition)]
               (build-flow-conditions* graph tail result*))))
