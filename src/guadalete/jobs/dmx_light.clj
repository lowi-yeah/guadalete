(ns guadalete.jobs.dmx-light
    (:require
      [onyx.api]
      [onyx.plugin.kafka]
      [taoensso.timbre :as log]
      [cheshire.core :as json]
      [onyx.schema :as os]
      [schema.core :as s]
      [guadalete.utils
       [job :refer [add-task add-tasks]]
       [config :as config :refer [kafka-topic]]
       [util :refer [now]]]
      [guadalete.tasks
       [kafka :as kafka-task]]))


(def base-job
  {:workflow       [[:read-messages :transform-messages]
                    [:transform-messages :write-messages]]
   :lifecycles     []
   :catalog        []
   :task-scheduler :onyx.task-scheduler/balanced})

(defn dmx->artnet [segment]
      (let [value (:data segment)]
           {:message {:id 0 :val value}}))

(defn hsb->dmx [segment]
      ;{:id "light-f00", :data [0.3 0.4 1]}
      (let [id (:id segment)
            value (:data segment)
            time (now)
            message [id value time]]
           ;(log/debug "hsb->dmx" (json/generate-string message))
           {:op :sadd :args ["guadalete/sgnl/raw" (json/generate-string message)]}))


(def transform
  {:task   {:task-map   {:onyx/name          :dmx-light/transform
                         :onyx/fn            ::hsb->dmx
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
                                            {:kafka/topic        (kafka-topic :light-value)
                                             :kafka/group-id     "light-value-consumer"
                                             :onyx/batch-size    1
                                             :onyx/batch-timeout 1000})
                          :lifecycle-opts {}}))
                     (add-task transform)

                     (add-task
                       (kafka-task/output-task
                         :write-messages
                         {:task-opts      (merge
                                            (config/kafka-task)
                                            {:kafka/topic        (kafka-topic :artnet)
                                             :onyx/batch-size    1
                                             :onyx/batch-timeout 1000}
                                            )
                          :lifecycle-opts {}}))
                     )]
           job*))

(defn build-job []
      {:name :light/dmx
       :job  (configure-job base-job)})