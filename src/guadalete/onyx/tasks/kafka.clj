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
       [task :as task-config]]

      [guadalete.utils.util :refer [pretty validate!]]))

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
                 :lifecycles [os/Lifecycle]}})

(defn- filtered-consumer
       "Helper for creating task that consume kafka messages."
       [{:keys [task-name signal-id] :as options}]
       (let [c (consumer options)
             flow-conditions [{:flow/from           task-name
                               :flow/to             :all
                               :flow/predicate      :guadalete.onyx.filters/signal-id
                               :flow/short-circuit? true}]
             consumer* (-> c
                           (assoc-in [:task :task-map :filter/signal-id] signal-id)
                           (assoc-in [:task :flow-conditions] flow-conditions)
                           (assoc-in [:schema :flow-conditions] [os/FlowCondition]))]
            consumer*))

(defn- producer
       "Helper for creating task that produce kafka messages."
       [{:keys [task-name group-id topic]}]

       (log/debug "KAFFKA producer" task-name group-id topic)

       {:task   {:task-map   (merge
                               (onyx-defaults)
                               (task-config/kafka-producer)
                               {:onyx/name      task-name
                                :onyx/plugin    :onyx.plugin.kafka/write-messages
                                :onyx/type      :output
                                :onyx/medium    :kafka
                                :onyx/doc       (str "Produces " (kafka-config/get-topic topic) " messages for Kafka")
                                :kafka/topic    (kafka-config/get-topic topic)
                                :kafka/group-id group-id})
                 :lifecycles [{:lifecycle/task  task-name
                               :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}]}
        :schema {:task-map   os/TaskMap
                 :lifecycles [os/Lifecycle]}})

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

(s/defn light-config-consumer
        "Task for consuming light configuration messages from kafka."
        ([task-name :- s/Keyword]
          (consumer {:task-name task-name
                     :group-id  "light-config-consumer"
                     :topic     :light-config})))

(s/defn light-producer
        "Task for producing color-messages for (mqtt) lights"
        ([task-name :- s/Keyword]
          (producer {:task-name task-name
                     :group-id  "light-producer"
                     :topic     :light-out})))