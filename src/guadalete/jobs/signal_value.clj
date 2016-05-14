(ns guadalete.jobs.signal-value
    (:require
      [onyx.api]
      [onyx.plugin.kafka]
      [onyx.plugin.redis]
      [onyx.tasks.redis :as redis-task]
      [taoensso.timbre :as log]
      [cheshire.core :as json]
      [onyx.schema :as os]
      [schema.core :as s]
      [guadalete.utils
       [job :refer [add-task add-tasks]]
       [config :as config]
       [util :refer [now]]]
      [guadalete.tasks
       [core-async :as core-async-task]
       [kafka :as kafka-task]]))


(def base-job
  {:workflow       [[:read-messages :transform-messages]
                    [:transform-messages :redis-messages]]
   :lifecycles     []
   :catalog        []
   :task-scheduler :onyx.task-scheduler/balanced})

(defn kafka->redis [segment]
      ;(log/debug "kafka->redis" segment)
      {:op :sadd :args ["guadalete/sgnl/raw" (json/generate-string (vals (assoc segment :t (now))))]})

(def transform-task
  {:task   {:task-map   {:onyx/name          :transform-messages
                         :onyx/fn            ::kafka->redis
                         :onyx/type          :function
                         :onyx/batch-size    10
                         :onyx/batch-timeout 400
                         :onyx/doc           "Prepares a raw signal message for logging to redis"}
            :lifecycles []}
   :schema {:task-map   os/TaskMap
            :lifecycles [os/Lifecycle]}}
  )

(defn configure-job
      [job]
      (let [job* (-> job
                     (add-task
                       (kafka-task/input-task
                         :read-messages
                         {:task-opts      (merge
                                            (config/kafka-task)
                                            {:kafka/topic "sgnl-v" :kafka/group-id "signal-value-consumer"})
                          :lifecycle-opts {}}))
                     (add-task transform-task)
                     (add-task (redis-task/writer :redis-messages (merge (config/onyx-defaults) (config/redis)))))]
           job*))

(defn build-job []
      {:name :sensor/value
       :job  (configure-job base-job)})