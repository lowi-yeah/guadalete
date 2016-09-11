(ns guadalete.onyx.jobs.base
    (:require
      [taoensso.timbre :as log]
      [guadalete.onyx.jobs.util :refer [empty-job add-tasks]]
      [guadalete.onyx.tasks.kafka :as kafka-tasks]
      [guadalete.onyx.tasks.redis :as redis-tasks]
      [guadalete.onyx.tasks.rethink :as rethink-tasks]
      [guadalete.onyx.tasks.logging :as log-tasks]
      [guadalete.config.kafka :as kafka-config]
      [guadalete.config.task :as taks-config]
      [guadalete.config.onyx :refer [onyx-defaults]]))

(defn signal-timeseries-consumer []
      (let [
            workflow [[:read-from-kafka :write-to-redis]]
            tasks [(kafka-tasks/signal-value-consumer :read-from-kafka "signal-value-consumer")
                   (redis-tasks/write-signals-timeseries :write-to-redis)]
            job (-> empty-job
                    (add-tasks tasks)
                    (assoc :workflow workflow))]
           {:name :signal/value-logger
            :job  job}))

(defn signal-config-consumer []
      (let [
            workflow [[:read-from-kafka :write-to-rethink]]
            tasks [(kafka-tasks/signal-config-consumer :read-from-kafka)
                   (rethink-tasks/output :write-to-rethink {:task-opts (onyx-defaults) :lifecycle-opts (merge (taks-config/rethink) {:rethinkdb/table "signal"})})]
            job (-> empty-job
                    (add-tasks tasks)
                    (assoc :workflow workflow))]
           {:name :signal/config-handler
            :job  job}))