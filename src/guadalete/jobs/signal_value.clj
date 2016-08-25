(ns guadalete.jobs.signal-value
    (:require
      [onyx.api]
      [onyx.plugin.kafka]
      [taoensso.timbre :as log]
      [cheshire.core :as json]
      [onyx.schema :as os]
      [schema.core :as s]
      [guadalete.utils
       [job :refer [add-task add-tasks]]
       [util :refer [now]]
       [redis-time-series :as ts]]
      [guadalete.config
       [task :as taks-config]
       [onyx :refer [onyx-defaults]]
       [kafka :refer [kafka-topic]]]
      [guadalete.tasks
       [kafka :as kafka-tasks]
       [redis :as redis-tasks]]))

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
                    [:read-messages :write-to-timeseries]
                    ;[:read-messages :log] [:log :write-to-timeseries]
                    ]
   :lifecycles     []
   :catalog        []
   :task-scheduler :onyx.task-scheduler/balanced})


(defn configure-job
      [job]
      (let [job* (-> job
                     (add-task
                       (kafka-tasks/input-task
                         :read-messages
                         {:task-opts      (merge
                                            (taks-config/kafka-consumer)
                                            {:kafka/topic        (kafka-topic :signal-value)
                                             :kafka/group-id     "signal-timeseries-consumer"
                                             :onyx/batch-size    1
                                             :onyx/batch-timeout 1000})
                          :lifecycle-opts {}}))

                     (add-task (log-task :log))
                     (add-task (redis-tasks/write-signals-timeseries :write-to-timeseries))
                     )]
           job*))

(defn build-job []
      {:name :signal/value
       :job  (configure-job base-job)})