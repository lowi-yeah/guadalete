(ns guadalete.jobs.signal-job
    (:require
      [aero.core :refer [read-config]]
      [onyx.plugin.kafka]
      [taoensso.timbre :as log]
      [guadalete.utils.job :refer [add-task add-tasks]]
      [guadalete.tasks
       [core-async :as core-async-task]
       [kafka :as kafka-task]
       [logging :as logging-behavior]
       [metrics :as metrics-behavior]]
      [onyx.api]))

(defn build-base-job
      "Build up the base job that will be shared in every environment configuration.
      Here you will build the workflow. Also build catalog entries, flow conditions,
      and lifecycles that apply to your business logic. The idea is to keep this
      clean of enviornment specific settings."
      [_opts]
      {:workflow       [[:read-messages :write-messages]]
       :lifecycles     []
       :catalog        []
       :task-scheduler :onyx.task-scheduler/balanced})

(defn configure-job
      "Configures the job to use either the :dev or :prod enviornments.
      This is where you will typically switch out your input/output plugins
      or enviornment specific settings like pinned tasks.
      However, the mode flag is disabled, as a real zookeeper/kafka setup is used for development as well (wirbelsturm)"
      [job mode {:keys [batch-size] :as opts}]
      (log/warn "ignoring :mode flag " mode)
      (let [job* (-> job
                     (add-task (kafka-task/input-task :read-messages
                                                      {:kafka/topic               "sgnl-v"
                                                       :kafka/group-id            "signal-consumer"
                                                       :kafka/fetch-size          307200
                                                       :kafka/chan-capacity       100
                                                       :kafka/zookeeper           "zookeeper1:2181"
                                                       :kafka/offset-reset        :largest
                                                       :kafka/force-reset?        true
                                                       :kafka/empty-read-back-off 50
                                                       :kafka/commit-interval     50
                                                       :kafka/deserializer-fn     :guadalete.tasks.kafka/deserialize-message-json
                                                       :kafka/wrap-with-metadata? false
                                                       :onyx/min-peers            1
                                                       :onyx/max-peers            1
                                                       :onyx/batch-size           batch-size}))

                     (add-task (core-async-task/output-task :write-messages {:onyx/batch-size batch-size}))
                     ;(logging-behavior/add-logging :write-messages)
                     ;(metrics-behavior/add-timbre-metrics job :read-messages)
                     )]
           job*))

(defn build-job [mode]
      (let [batch-size 10
            batch-timeout 1000]
           (-> (build-base-job {:batch-size batch-size :batch-timeout batch-timeout})
               (configure-job mode {:batch-size batch-size :batch-timeout batch-timeout}))))