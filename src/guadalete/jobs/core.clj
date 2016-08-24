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
       [signal :as signal-task]
       [items :as item-tasks]]

      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.schema.core :refer [FlowMap Flow Scene JobMap Room Light Signal]]
      [guadalete.utils.config :as config]
      [guadalete.tasks.async :as async]))

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
           (log/error "ERROR" (.getMessage e)))))

(defn base-job []
      {:workflow       []
       :lifecycles     []
       :catalog        []
       :triggers       []
       :windows        []
       :task-scheduler :onyx.task-scheduler/balanced})

(s/defn add-task :- os/Job
        "Adds a task's task-definition to a job"
        [{:keys [lifecycles triggers windows flow-conditions] :as job}
         {:keys [task schema] :as task-definition}]

        (log/debug "ass task" (pretty task))

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

(defmethod task-from-item :signal [[id signal]]
           (item-tasks/signal id))

(defmethod task-from-item :light [[id light]]
           (item-tasks/light id))

(defmethod task-from-item :color [[id color]]
           (item-tasks/color id))

(defn- graph-tasks*
       "Recursive helper for graph jobs."
       [graph nodes result]
       (if (empty? nodes)
         result
         (let [[head & tail] nodes
               item (uber/attrs graph head)
               task (task-from-item [head item])
               result* (assoc result head task)]
              (graph-tasks* graph tail result*))))

(defn- make-workflow
       "Internal helper for creating the jobs workflow.
       It's easy as… Just get the edges of the graph represented as two-dimensional vectors [:src :dest]"
       [graph]
       (->>
         graph
         (uber/edges)
         (map (fn [e] [(:src e) (:dest e)]))
         (into [])))

(defn- graph-jobs [[scene-id graph]]
       (let [topological-ordering (alg/topsort graph)
             tasks (graph-tasks* graph topological-ordering {})
             workflow (make-workflow graph)
             job (->
                   (base-job)
                   (add-tasks (vals tasks))
                   (assoc :workflow workflow))]
            {:name scene-id
             :job  job}))

;(s/defn ^:always-validate from-flows :- s/Any
(s/defn from-flows :- s/Any
        "Function for creating onyx jobs form signal flows."
        [flows :- FlowMap]
        (let [graph-map (graph/make-graphs flows)]
             (->> graph-map
                  (map #(graph-jobs %))
                  (into ()))))