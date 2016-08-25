(ns guadalete.onyx.tasks.kafka
    (:require
      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.schema.onyx :refer [KafkaInputTask]]
      [taoensso.timbre :as log]

      [guadalete.config
       [onyx :refer [onyx-defaults]]
       [kafka :as kafka-config]
       [task :as task-config]
       ]
      ))


;(s/defn input-task
;        [task-name :- s/Keyword
;         {:keys [task-opts lifecycle-opts] :as opts}]
;
;        (log/debug "input- kafka task options" task-opts)
;
;        {:task   {:task-map   (merge {:onyx/name   task-name
;                                      :onyx/plugin :onyx.plugin.kafka/read-messages
;                                      :onyx/type   :input
;                                      :onyx/medium :kafka
;                                      :onyx/doc    "Reads messages from a Kafka topic"}
;                                     task-opts)
;                  :lifecycles [(merge
;                                 {:lifecycle/task  task-name
;                                  :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}
;                                 lifecycle-opts)]}
;         :schema {:task-map   (merge os/TaskMap KafkaInputTask)
;                  :lifecycles [os/Lifecycle]}})
;
;(s/defn output-task
;        [task-name :- s/Keyword
;         {:keys [task-opts lifecycle-opts] :as opts}]
;        (log/debug "output- kafka task options" task-opts)
;
;        {:task   {:task-map   (merge {:onyx/name   task-name
;                                      :onyx/plugin :onyx.plugin.kafka/write-messages
;                                      :onyx/type   :output
;                                      :onyx/medium :kafka
;                                      :onyx/doc    "Writes messages to a Kafka topic"}
;                                     task-opts)
;                  :lifecycles [(merge {:lifecycle/task  task-name
;                                       :lifecycle/calls :onyx.plugin.kafka/write-messages-calls}
;                                      lifecycle-opts)]}
;         :schema {:task-map   (merge os/TaskMap KafkaOutputTask)
;                  :lifecycles [os/Lifecycle]}})



(s/defn signal-value-consumer
        [task-name :- s/Keyword]
        (log/debug "signal-value-consumer" task-name)
        {:task   {:task-map   (merge
                                (onyx-defaults)
                                (task-config/kafka-consumer)
                                {:onyx/name   task-name
                                 :onyx/plugin :onyx.plugin.kafka/read-messages
                                 :onyx/type   :input
                                 :onyx/medium :kafka
                                 :onyx/doc    "Reads messages from a Kafka topic"}
                                {:kafka/topic    (kafka-config/get-topic :signal-value)
                                 :kafka/group-id "signal-timeseries-consumer"})


                  :lifecycles [{:lifecycle/task  task-name
                                :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}]}

         :schema {:task-map   os/TaskMap
                  :lifecycles [os/Lifecycle]}})