(ns guadalete.jobs.switch-config
    (:require
      [onyx.api]
      [onyx.plugin.kafka]
      [taoensso.timbre :as log]
      [guadalete.utils
       [job :refer [add-task add-tasks]]]
      [guadalete.config
       [task :as config]
       [onyx :refer (onyx-defaults)]
       [kafka :refer [kafka-topic]]
       ]
      [guadalete.tasks
       [core-async :as core-async-task]
       [kafka :as kafka]
       [rethink :as rethink]]))


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
                       (kafka/input-task
                         :read-switch-config
                         {:task-opts      (merge
                                            (config/kafka-consumer)
                                            {:kafka/topic    (kafka-topic :switch-config)
                                             :kafka/group-id "switch-config-consumer"})
                          :lifecycle-opts {}}))

                     (add-task
                       (rethink/output-task
                         :save-switch-config
                         {:task-opts      (onyx-defaults)
                          :lifecycle-opts (merge (config/rethink) {:rethinkdb/table "switch"})}))

                     ;(add-task (core-async-task/output-task :write-signal-config {:onyx/batch-size batch-size}))
                     )]
           job*))

(defn build-job []
      (let [job (-> (build-base-job {})
                    (configure-job {}))]
           {:name :switch/config
            :job  job}))