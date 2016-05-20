(ns guadalete.utils.config
    (:require [environ.core :refer [env]]))

(defn- get*
       [key] (get env key))

(defn- get-bool
       [key] (Boolean/valueOf (get env key)))

(defn onyx []
      {
       :n-peers     (get* :onyx.peer/n-peers)
       ;:n-peers     16
       :peer-config {:zookeeper/address                     (get* :zookeeper/address)
                     :onyx.messaging/impl                   (get* :onyx.messaging/impl)
                     :onyx.peer/job-scheduler               (get* :onyx.peer/job-scheduler)
                     :onyx.messaging/peer-port              (get* :onyx.messaging/peer-port)
                     :onyx.messaging/bind-addr              (get* :onyx.messaging/bind-addr)
                     :onyx.log/config                       (get* :onyx.log/config)
                     :onyx.messaging.aeron/embedded-driver? (get-bool :onyx.messaging.aeron/embedded-driver?)}})

(def onyx-batch*
  {:batch-size    10
   :batch-timeout 1000})

(defn onyx-batch []
      {:onyx/batch-size    (:batch-size onyx-batch*)
       :onyx/batch-timeout (:batch-timeout onyx-batch*)})

(defn onyx-peer []
      {:onyx/min-peers 1
       :onyx/max-peers 1})

(defn onyx-defaults []
      (merge (onyx-peer) (onyx-batch)))

(def kafka-prefix "gdlt-")

(def kafka-topics
  {:signal-value  {:name               "gdlt-sgnl-v"
                   :partitions         1
                   :replication-factor 1
                   :config             {"cleanup.policy" "compact"}}
   :signal-config {:name               "gdlt-sgnl-c"
                   :partitions         1
                   :replication-factor 1
                   :config             {"cleanup.policy" "compact"}}
   :switch-value  {:name               "gdlt-swtch-v"
                   :partitions         1
                   :replication-factor 1
                   :config             {"cleanup.policy" "compact"}}
   :switch-config {:name               "gdlt-swtch-c"
                   :partitions         1
                   :replication-factor 1
                   :config             {"cleanup.policy" "compact"}}
   :artnet        {:name               "gdlt-artnet"
                   :partitions         1
                   :replication-factor 1
                   :config             {"cleanup.policy" "compact"}}})



(defn kafka-topic [topic]
      (get-in kafka-topics [topic :name]))

(defn kafka []
      {:zookeeper-address (get* :zookeeper/address)
       :kafka-topics      (vals kafka-topics)})


(defn rethinkdb []
      {:host     (get* :rethinkdb/host)
       :port     (get* :rethinkdb/port)
       :auth-key (get* :rethinkdb/auth-key)
       :db       (get* :rethinkdb/db)
       :tables   (get* :rethinkdb/tables)})

(defn redis []
      {:redis/uri             (get* :redis/uri)
       :redis/read-timeout-ms (get* :redis/read-timeout-ms)})

(defn mqtt []
      {:mqtt-broker (get* :mqtt/broker)
       :mqtt-id     (get* :mqtt/id)
       :mqtt-topics (get* :mqtt/topics)})

(defn artnet []
      {:node-port         7000
       :poll-interval     10000
       :broadcast-address "255.255.255.255"
       :server-port       6454})

(defn signal-configuration []
      (merge (rethinkdb) {:table "signal"}))

(defn signal-value []
      (merge (redis) {:batch-size 10}))

(defn weather-signal []
      (merge (mqtt) {
                     ;:value-update-interval  120000        ; two minutes
                     :value-update-interval  (* 60 1000)
                     :config-update-interval (* 60 1000)
                     :parameters             ["temperature" "windSpeed" "cloudCover" "pressure" "ozone" "humidity"]
                     :config-map             {:type "analog"}
                     }))

(defn weather-signal []
      (merge (mqtt) {
                     ;:value-update-interval  120000        ; two minutes
                     :value-update-interval  (* 60 1000)
                     :config-update-interval (* 60 1000)
                     :parameters             ["temperature" "windSpeed" "cloudCover" "pressure" "ozone" "humidity"]
                     :config-map             {:type "analog"}
                     }))

(defn kafka-task
      "Default configuration for onyx-kafka tastks"
      []
      (merge (onyx-defaults)
             {
              :kafka/fetch-size          307200
              :kafka/chan-capacity       100
              :kafka/zookeeper           (get* :zookeeper/address)
              :kafka/offset-reset        :largest
              :kafka/force-reset?        true
              :kafka/empty-read-back-off 50
              :kafka/commit-interval     50
              :kafka/deserializer-fn     :guadalete.tasks.kafka/deserialize-message-json
              :kafka/serializer-fn       :guadalete.tasks.kafka/serialize-message-json
              :kafka/wrap-with-metadata? false})
      )

(defn rethink-task []
      {:rethinkdb/host     (get* :rethinkdb/host)
       :rethinkdb/port     (get* :rethinkdb/port)
       :rethinkdb/auth-key (get* :rethinkdb/auth-key)
       :rethinkdb/db       (get* :rethinkdb/db)
       :rethinkdb/tables   (get* :rethinkdb/tables)})

