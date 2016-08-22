(ns guadalete.jobs.signal-value
    (:require
      [onyx.api]
      [onyx.plugin.kafka]
      [taoensso.carmine :as car]
      [taoensso.timbre :as log]
      [cheshire.core :as json]
      [onyx.schema :as os]
      [schema.core :as s]
      [clj-uuid :as uuid]
      [guadalete.utils
       [job :refer [add-task add-tasks]]
       [util :refer [now]]
       [redis-time-series :as ts]]
      [guadalete.config
       [task :as taks-config]
       [onyx :refer [onyx-defaults]]
       [kafka :refer [kafka-topic]]]
      [guadalete.tasks
       [async :as async]
       [kafka :as kafka]
       [rethink :as rethink]
       [redis :as redis]]
      [guadalete.jobs.state :as state]))



(defn log! [segment]
      (log/debug "log!" segment)
      segment)

(defn log-task
      [task-name]
      {:task   {:task-map   (merge
                              {:onyx/name task-name
                               :onyx/fn   ::log!
                               :onyx/type :function
                               :onyx/doc  "Writes segments to a core.async channel"}
                              (onyx-defaults))
                :lifecycles []}
       :schema {:task-map   os/TaskMap
                :lifecycles [os/Lifecycle]}})


(def base-job
  {:workflow       [
                    ;[:read-messages :log] [:log :write-messages]
                    [:read-messages :write-messages]
                    [:read-messages :publish-async]]
   :lifecycles     []
   :catalog        []
   :task-scheduler :onyx.task-scheduler/balanced})


(defn configure-job
      [job]
      (let [job* (-> job
                     (add-task
                       (kafka/input-task
                         :read-messages
                         {:task-opts      (merge
                                            (taks-config/kafka)
                                            {:kafka/topic        (kafka-topic :signal-value)
                                             :kafka/group-id     "signal-value-consumer"
                                             :onyx/batch-size    1
                                             :onyx/batch-timeout 1000})
                          :lifecycle-opts {}}))

                     (add-task (log-task :log))

                     (add-task
                       (redis/output-task
                         :write-messages
                         {:task-opts      (onyx-defaults)
                          :lifecycle-opts (merge (taks-config/redis) {:redis/prefix "sgnl"})}))

                     (add-task
                       (async/publish-task
                         :publish-async
                         {:task-opts      (onyx-defaults)
                          :lifecycle-opts {}}))
                     )]
           job*))

(defn build-job []
      {:name :signal/value
       :job  (configure-job base-job)})