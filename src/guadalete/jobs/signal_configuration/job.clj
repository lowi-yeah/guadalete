(ns guadalete.jobs.signal-configuration.job
    (:require
      [onyx.plugin.kafka]
      [taoensso.timbre :as log]
      [guadalete.utils.job :refer [add-task add-tasks]]
      [guadalete.utils.config :as config]
      [guadalete.tasks
       [core-async :as core-async-task]
       [kafka :as kafka-task]
       [rethink :as rethink-task]
       ]
      [onyx.api]))


(defn build-base-job
      [_opts]
      {:workflow       [[:read-signal-config :save-signal-config]]
       :lifecycles     []
       :catalog        []
       :task-scheduler :onyx.task-scheduler/balanced})

(defn configure-job
      [job rethink-config opts]
      (let [job* (-> job
                     (add-task (kafka-task/input-task :read-signal-config
                                                      {:kafka/topic               "sgnl-c" ; TODO: parametrize
                                                       :kafka/group-id            "signal-consumer" ; TODO: parametrize
                                                       :kafka/fetch-size          307200
                                                       :kafka/chan-capacity       100
                                                       :kafka/zookeeper           "zookeeper1:2181" ; TODO: parametrize
                                                       :kafka/offset-reset        :largest
                                                       :kafka/force-reset?        true
                                                       :kafka/empty-read-back-off 50
                                                       :kafka/commit-interval     50
                                                       ;:kafka/deserializer-fn     :guadalete.tasks.kafka/deserialize-message-edn
                                                       ;:kafka/deserializer-fn     :guadalete.tasks.kafka/deserialize-message-string
                                                       :kafka/deserializer-fn     :guadalete.tasks.kafka/deserialize-message-json
                                                       :kafka/wrap-with-metadata? false
                                                       :onyx/min-peers            1 ; TODO: parametrize
                                                       :onyx/max-peers            1 ; TODO: parametrize
                                                       :onyx/batch-size           (:batch-size opts)}))

                     (add-task (rethink-task/output-task :save-signal-config rethink-config {:onyx/batch-size    (:batch-size opts)
                                                                                             :onyx/batch-timeout (:batch-timeout opts)}))

                     ;(add-task (core-async-task/output-task :write-signal-config {:onyx/batch-size batch-size}))
                     )]
           job*))

(defn build-job [rethink-config]
      (log/debug "building signal config job" rethink-config)
      (let [batch-size 10
            batch-timeout 1000]
           (-> (build-base-job {})
               (configure-job rethink-config {:batch-size batch-size :batch-timeout batch-timeout}))))