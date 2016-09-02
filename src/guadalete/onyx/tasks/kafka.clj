(ns guadalete.onyx.tasks.kafka
    (:require
      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.schema.onyx :refer [KafkaInputTask]]
      [taoensso.timbre :as log]
      [guadalete.onyx.tasks.util]

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

(defn- consumer
       "Helper for creating task that consume kafka messages."
       [{:keys [task-name group-id topic]}]
       {:task   {:task-map   (merge
                               (onyx-defaults)
                               (task-config/kafka-consumer)
                               {:onyx/name   task-name
                                :onyx/plugin :onyx.plugin.kafka/read-messages
                                :onyx/type   :input
                                :onyx/medium :kafka
                                :onyx/doc    (str "Consumes " (kafka-config/get-topic topic) " messages from Kafka")}
                               {:kafka/topic    (kafka-config/get-topic topic)
                                :kafka/group-id group-id})
                 :lifecycles [{:lifecycle/task  task-name
                               :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}]}

        :schema {:task-map   os/TaskMap
                 :lifecycles [os/Lifecycle]}}
       )

(defn- filtered-consumer
       "Helper for creating task that consume kafka messages."
       [{:keys [task-name signal-id] :as options}]
       (let [c (consumer options)
             filter-lifecycle {:lifecycle/task  task-name
                               :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}]
            (assoc-in c [:task :task-map :filter/signal-id] signal-id)))


(s/defn signal-value-consumer
        "Task for consuming signal values from kafka."
        ([task-name :- s/Keyword
          group-id :- s/Str]
          (consumer {:task-name task-name
                     :group-id  group-id
                     :topic     :signal-value}))
        ([task-name :- s/Keyword
          group-id :- s/Str
          signal-id :- s/Str]
          (filtered-consumer {:task-name task-name
                              :group-id  group-id
                              :topic     :signal-value
                              :signal-id signal-id})))

(s/defn signal-config-consumer
        "Task for consuming signal values from kafka."
        ([task-name :- s/Keyword]
          (consumer {:task-name task-name
                     :group-id  "signal-config-consumer"
                     :topic     :signal-config})))
