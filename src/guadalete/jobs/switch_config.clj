(ns guadalete.jobs.switch-config
    (:require
      [onyx.plugin.kafka]
      [taoensso.timbre :as log]
      [guadalete.utils
       [job :refer [add-task add-tasks]]
       [config :as config]]
      [guadalete.tasks
       [core-async :as core-async-task]
       [kafka :as kafka-task]
       [rethink :as rethink-task]
       ]
      [onyx.api]))


(defn build-base-job
      [_opts]
      {:workflow       [[:read-switch-config :save-switch-config]]
       :lifecycles     []
       :catalog        []
       :task-scheduler :onyx.task-scheduler/balanced})

(defn configure-job
      [job _opts]
      (let [job* (-> job
                     (add-task
                       (kafka-task/input-task
                         :read-switch-config
                         {:task-opts      (merge
                                            (config/kafka-task)
                                            {:kafka/topic "swtch-c" :kafka/group-id "switch-config-consumer"})
                          :lifecycle-opts {}}))

                     (add-task
                       (rethink-task/output-task
                         :save-switch-config
                         {:task-opts      (config/onyx-defaults)
                          :lifecycle-opts (merge (config/rethink-task) {:rethinkdb/table "switch"})}))

                     ;(add-task (core-async-task/output-task :write-signal-config {:onyx/batch-size batch-size}))
                     )]
           job*))

(defn build-job []
      (let [job (-> (build-base-job {})
                    (configure-job {}))]
           {:name :switch/config
            :job  job}))