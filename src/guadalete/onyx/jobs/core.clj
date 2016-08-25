(ns guadalete.onyx.jobs.core
    (:require
      [onyx.api]
      [onyx.plugin.kafka]
      [taoensso.timbre :as log]
      [onyx.schema :as os]
      [schema.core :as s]
      [guadalete.utils.util :refer [pretty]]
      ))

(def empty-job
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

        (log/debug "add task" (pretty task))

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
