(ns guadalete.onyx.jobs.scene
    (:require
      [ubergraph.core :as uber]
      [ubergraph.alg :as alg]
      [taoensso.timbre :as log]

      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.schema.core :as gs]
      [guadalete.graph.core :as graph]
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

(defn- make-flows
       "Internal helper for creating the flows of a job.
       It's easy as… Just get the edges of the graph represented as two-dimensional vectors [:src :dest]"
       [graph]
       (->>
         graph
         (uber/edges)
         (map (fn [e] [(:src e) (:dest e)]))
         (into [])))

(s/defn make-job :- os/Job
        "Creates an onyx job from a given scene-graph"
        [[scene-id graph]]

        (log/debug "\n\nMake graph job:")
        (uber/pprint graph)

        (let [topological-ordering (into [] (alg/topsort graph))
              flows (make-flows graph)

              ;_ (log/debug "topological-ordering" topological-ordering)
              _ (log/debug "flows" flows)

              catalog (build-catalog* graph topological-ordering flows [])
              flow-conditions (build-flow-conditions* graph (uber/edges graph) [])

              _ (log/debug "flow-conditions" (into [] flow-conditions))

              job (-> empty-job
                      (add-tasks catalog)
                      (add-flow-conditions flow-conditions)
                      (assoc :workflow flows))
              ]

             (log/debug "job" job)

             {:name scene-id
              :job  job}
             ))

(s/defn ^:always-validate from-graphs :- s/Any
        ;(s/defn from-graphs :- s/Any
        "Function for creating onyx jobs form signal flows."
        [graph-map :- gs/GraphMap]
        (let [graph-jobs (->> graph-map
                              (map #(make-job %))
                              (into ()))]
             graph-jobs))
