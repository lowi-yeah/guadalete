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
       [config :as config :refer [kafka-topic]]
       [util :refer [now]]]
      [guadalete.tasks
       [core-async :as core-async-task]
       [kafka :as kafka-task]]))


(def base-job
  {:workflow       [[:read-messages :redis-transform]
                    [:redis-transform :redis-messages]
                    ;debug: write signal values direcly into artnet
                    ;[:read-messages :artnet-transform]
                    ;[:artnet-transform :artnet-messages]
                    ]
   :lifecycles     []
   :catalog        []
   :task-scheduler :onyx.task-scheduler/balanced})

(defn kafka->redis [segment]
      ;{:id "sin3", :data 0}
      (let [id (:id segment)
            value (:data segment)
            time (now)
            message [id value time]]
           ;(log/debug "kafka->redis" (json/generate-string message))
           {:op :sadd :args ["guadalete/sgnl/raw" (json/generate-string message)]}))

(def redis-transform
  {:task   {:task-map   {:onyx/name          :redis-transform
                         :onyx/fn            ::kafka->redis
                         :onyx/type          :function
                         :onyx/batch-size    10
                         :onyx/batch-timeout 400
                         :onyx/doc           "Prepares a raw signal message for logging to redis"}
            :lifecycles []}
   :schema {:task-map   os/TaskMap
            :lifecycles [os/Lifecycle]}})

(defn signal->artnet [segment]
      (let [value (:data segment)]
           {:message {:id 0 :val value}}))

(def artnet-transform
  {:task   {:task-map   {:onyx/name          :artnet-transform
                         :onyx/fn            ::signal->artnet
                         :onyx/type          :function
                         :onyx/batch-size    1
                         :onyx/batch-timeout 1000
                         :onyx/doc           "Prepares a raw signal message for sending to artnet (via kafka)"}
            :lifecycles []}
   :schema {:task-map   os/TaskMap
            :lifecycles [os/Lifecycle]}})


(defn configure-job
      [job]
      (let [job* (-> job
                     (add-task
                       (kafka-task/input-task
                         :read-messages
                         {:task-opts      (merge
                                            (config/kafka-task)
                                            {:kafka/topic        (kafka-topic :signal-value)
                                             :kafka/group-id     "signal-value-consumer"
                                             :onyx/batch-size    1
                                             :onyx/batch-timeout 1000})
                          :lifecycle-opts {}}))
                     (add-task redis-transform)
                     (add-task (redis-task/writer :redis-messages (merge (config/onyx-defaults) (config/redis))))

                     ;(add-task artnet-transform)
                     ;(add-task
                     ;  (kafka-task/output-task
                     ;    :artnet-messages
                     ;    {:task-opts      (merge
                     ;                       (config/kafka-task)
                     ;                       {:kafka/topic (kafka-topic :artnet)
                     ;                        :onyx/batch-size    1
                     ;                        :onyx/batch-timeout 1000}
                     ;                       )
                     ;     :lifecycle-opts {}}))
                     )]
           job*))

(defn build-job []
      {:name :signal/value
       :job  (configure-job base-job)})