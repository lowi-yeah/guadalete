(ns guadalete.onyx.jobs.base
    (:require
      [taoensso.timbre :as log]
      [guadalete.onyx.jobs.core :refer [empty-job add-tasks]]
      [guadalete.onyx.tasks.kafka :as kafka-tasks]
      [guadalete.onyx.tasks.redis :as redis-tasks]
      [guadalete.config.kafka :as kafka-config]))


(defn signal-timeseries-consumer []
      (let [
            workflow [[:read-from-kafka :write-to-redis]]

            options {:kafka/topic    (kafka-config/get-topic :signal-value)
                     :kafka/group-id "signal-timeseries-consumer"}
            tasks [
                   (kafka-tasks/signal-value-consumer :read-from-kafka)
                   (redis-tasks/write-signals-timeseries :write-to-redis)
                   ]

            job (-> empty-job
                    (add-tasks tasks)
                    (assoc :workflow workflow))

            ]
           (log/debug "signal-timeseries-consumer:" job)
           {:name :signal/value-logger
            :job  job}
           ))