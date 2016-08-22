(ns guadalete.jobs.core
    (:require
      [ubergraph.core :as uber]
      [ubergraph.alg :as alg]
      [taoensso.timbre :as log]
      [guadalete.jobs.graph :as graph]
      [guadalete.utils.util :refer [pretty]]
      [guadalete.tasks
       [core-async :as core-async-task]
       [redis :as redis-task]
       [kafka :as kafka-task]
       [signal :as signal-task]]
      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.schema.core :refer [FlowMap Flow Scene JobMap Room Light Signal]]
      [guadalete.utils.config :as config]
      ))

(def task-schema
  {:task-map   os/TaskMap
   :lifecycles [os/Lifecycle]})

(defn- validate!
       [schema data]
       (try
         (log/debug "validate" data)
         (log/debug "schema" schema)
         (s/validate schema data)
         (log/debug (str "valid " schema ":") data)
         (catch Exception e
           (log/error "ERROR" (.getMessage e))))
       )

(defn base-job []
      {:workflow       []
       :lifecycles     []
       :catalog        []
       :task-scheduler :onyx.task-scheduler/balanced})

(s/defn add-task :- os/Job
        "Adds a task's task-definition to a job"
        [{:keys [lifecycles triggers windows flow-conditions] :as job}
         {:keys [task schema] :as task-definition}]
        (when schema (s/validate schema task))
        (cond-> job
                true (update :catalog conj (:task-map task))
                lifecycles (update :lifecycles into (:lifecycles task))
                triggers (update :triggers into (:triggers task))
                windows (update :windows into (:windows task))
                flow-conditions (update :flow-conditions into (:flow-conditions task))))

(defn add-tasks
      "Same thing as add-task, but accepts a collection of tasks"
      ([job tasks]
        (reduce
          (fn [job task]
              (add-task job task)) job tasks)))

(defmulti task-from-item
          (fn [[id {:keys [ilk]}]] ilk))

(defmethod task-from-item :signal
           [[id signal]]
           (let [
                 task (signal-task/read-value signal)]

                (validate!
                  (:schema task)
                  (:task task))

                [id task]))

(defmethod task-from-item :light
           [[id light]]
           (let [task {:task   {:task-map   {:onyx/name       :log-signal-config
                                             :onyx/fn         ::log!
                                             :onyx/type       :function
                                             :onyx/batch-size 1}
                                :lifecycles []}
                       :schema {:task-map   os/TaskMap
                                :lifecycles [os/Lifecycle]}}]
                (log/debug ":light task from node" task)
                [id task]))

(defmethod task-from-item :color
           [[id color]]
           (let [task {:task   {:task-map   {:onyx/name       :log-signal-config
                                             :onyx/fn         ::log!
                                             :onyx/type       :function
                                             :onyx/batch-size 1}
                                :lifecycles []}
                       :schema {:task-map   os/TaskMap
                                :lifecycles [os/Lifecycle]}}]
                (log/debug ":color task from node" task)
                [id task]))

(defn- graph-jobs [[scene-id graph] ]
       (uber/pprint graph)
       (let [sources (->> graph
                          (graph/source-nodes)
                          (map (fn [node] [node (uber/attrs graph node)]))
                          (into {}))
             leaves (->> graph
                         (graph/leaf-nodes)
                         (map (fn [node] [node (uber/attrs graph node)]))
                         (into {}))

             source-tasks (->> sources
                               (map task-from-item)
                               (into {}))

             job (-> (base-job)
                     (add-tasks (vals source-tasks)))
             ]

            ;(try
            ;  (s/validate os/Job job)
            ;  (log/debug "valid job!")
            ;  (catch Exception e
            ;    (log/error "ERRORR!" (.getMessage e))))

            (log/debug "source-tasks" source-tasks)
            ;(log/debug "sources" sources)
            ;(log/debug "leaves" leaves)
            (log/debug "job" (pretty job))
            []))

;(s/defn ^:always-validate from-flows :- s/Any
(s/defn from-flows :- s/Any
        "Function for creating onyx jobs form signal flows."
        [flows :- FlowMap]

        ;(validate! FlowMap flows)

        (let [graph-map (graph/make-graphs flows)
              ;_ (log/debug "graph-map" graph-map)
              jobs (->> graph-map
                        (map #(graph-jobs % ))
                        (into []))]

             (log/debug "jobs" jobs)

             ;(doall
             ;  (map (fn [[scene-id g]]
             ;           (log/debug "graphs for scene:" scene-id)
             ;           (let [
             ;                 sources (graph/source-nodes g)
             ;                 leaves (graph/leaf-nodes g)
             ;                 search-specification {:start-nodes sources}
             ;                 shortest-paths (alg/shortest-path g search-specification)
             ;                 ]
             ;
             ;                ;(uber/pprint ts)
             ;                ;(log/debug (into [] ts))
             ;                (log/debug "sources:" (into [] sources))
             ;                (log/debug "leaves:" (into [] leaves))
             ;                (log/debug "shortest-paths:" (pretty shortest-paths))
             ;                ;(log/debug "spanning-tree: " (pretty spanning-tree))
             ;                ;(log/debug "nodes" (pretty nodes))
             ;                ))
             ;       graph-map))
             ;(log/debug "graphs" (str graphs))
             ;(log/debug "flows" (pretty flows))
             ;(log/debug "lights" (pretty lights))

             []
             )
        )