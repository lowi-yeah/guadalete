(ns guadalete.jobs.signal-config
    (:require
      [onyx.plugin.kafka]
      [taoensso.timbre :as log]
      [guadalete.utils
       [job :refer [add-task add-tasks]]
       [config :as config]]
      [guadalete.tasks
       [core-async :as core-async-task]
       [kafka :as kafka-task]
       [rethink :as rethink-task]]
      [onyx.api]))


(defn build-base-job
      [_opts]
      {:workflow       [[:read-signal-config :save-signal-config]]
       :lifecycles     []
       :catalog        []
       :task-scheduler :onyx.task-scheduler/balanced})

(defn configure-job
      [job _opts]
      (-> job
          (add-task
            (kafka-task/input-task
              :read-signal-config
              {:task-opts      (merge
                                 (config/kafka-task)
                                 {:kafka/topic "sgnl-c" :kafka/group-id "signal-config-consumer"})
               :lifecycle-opts {}}
              ))
          (add-task
            (rethink-task/output-task
              :save-signal-config
              {:task-opts      (config/onyx-defaults)
               :lifecycle-opts (merge (config/rethink-task) {:rethinkdb/table "signal"})}))))


(defn build-job []
      (let [job (-> (build-base-job {})
                    (configure-job {}))]
           {:name :signal/config
            :job  job}))