(ns guadalete.config.task
    (:require
      [guadalete.config.environment :as env]
      [guadalete.config.onyx :refer [onyx-defaults]]))


(defn kafka
      "Default configuration for onyx-kafka tastks"
      []
      (merge (onyx-defaults)
             {
              :kafka/fetch-size          307200
              :kafka/chan-capacity       100
              :kafka/zookeeper           (env/get-value :zookeeper/address)
              :kafka/offset-reset        :largest
              :kafka/force-reset?        true
              :kafka/empty-read-back-off 50
              :kafka/commit-interval     50
              :kafka/deserializer-fn     :guadalete.tasks.kafka/deserialize-message-json
              :kafka/serializer-fn       :guadalete.tasks.kafka/serialize-message-json
              :kafka/wrap-with-metadata? false}))

(defn rethink []
      {:rethinkdb/host     (env/get-value :rethinkdb/host)
       :rethinkdb/port     (env/get-value :rethinkdb/port)
       :rethinkdb/auth-key (env/get-value :rethinkdb/auth-key)
       :rethinkdb/db       (env/get-value :rethinkdb/db)
       :rethinkdb/tables   (env/get-value :rethinkdb/tables)})

(defn redis []
      {:redis/uri             (env/get-value :redis/uri)
       :redis/read-timeout-ms (env/get-value :redis/read-timeout-ms)
       :redis/prefix          "sgnl"})
