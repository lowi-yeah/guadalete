(ns guadalete.onyx.jobs.scene
    (:require
      [ubergraph.core :as uber]
      [ubergraph.alg :as alg]
      [taoensso.timbre :as log]

      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.schema.core :as gs]

      [guadalete.onyx.tasks.items.color]
      [guadalete.onyx.tasks.items.light]
      [guadalete.onyx.tasks.items.mixer]
      [guadalete.onyx.tasks.items.signal]
      [guadalete.onyx.filters]

      [guadalete.onyx.tasks.scene :refer [node-task]]
      [guadalete.onyx.jobs.util :refer [empty-job add-task add-tasks add-flow-conditions]]
      [guadalete.config.graph :as graph-config]
      [guadalete.utils.util :refer [pretty validate!]]))


(s/defn make-task :- os/TaskMap
        [graph
         node-id :- s/Keyword]
        (log/debug "make-task" node-id)
        (let [attrs (uber/attrs graph node-id)
              ;_ (log/debug "\t attrs" attrs)
              fn-symbol (symbol (namespace (:task attrs)) (name (:task attrs)))
              ;_ (log/debug "\t fn-symbol" fn-symbol)
              function (resolve fn-symbol)
              ;_ (log/debug "\t function" function)
              task-map (function attrs)]
             task-map))


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
         catalog]
        (if (empty? nodes)
          (do
            catalog)
          (let [[head & tail] nodes
                task (make-task graph head)
                catalog* (conj catalog task)]
               (build-catalog* graph tail catalog*))))

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

(s/defn make-job-from-graph :- os/Job
        [{:keys [scene-id graph]}]

        (log/debug "**** make-job-from-graph")
        (uber/pprint graph)
        (let [topological-ordering (alg/topsort graph)
              ;_ (log/debug "topological-ordering")
              catalog (build-catalog* graph topological-ordering [])
              ;_ (log/debug "catalog" (->> catalog
              ;                            (map #(get % :task))
              ;                            (into [])
              ;                            (pretty)))

              flows (make-flows graph)

              job (-> empty-job
                      (add-tasks catalog)
                      (assoc :workflow flows))]
             {:name scene-id
              :job  job}))

(s/defn from-graphs
        [graphs]
        (->> graphs
             (map make-job-from-graph)
             (into [])))
