(ns guadalete.jobs.signal-config
    (:require
      [onyx.api]
      [onyx.plugin.kafka]
      [onyx.schema :as os]
      [taoensso.timbre :as log]
      [guadalete.utils
       [job :refer [add-task add-tasks]]]
      [guadalete.config
       [task :as taks-config]
       [onyx :refer [onyx-defaults]]
       [kafka :refer [kafka-topic]]]
      [guadalete.tasks
       [core-async :as core-async-task]
       [kafka :as kafka]
       [rethink :as rethink]]))


(defn build-base-job
      [_opts]
      {:workflow       [[:read-signal-config :save-signal-config]]
       :lifecycles     []
       :catalog        []
       :task-scheduler :onyx.task-scheduler/balanced})

(defn log! [segment]
      (log/debug "signal-config" (str segment))
      segment)

(def log
  {:task   {:task-map   {:onyx/name       :log-signal-config
                         :onyx/fn         ::log!
                         :onyx/type       :function
                         :onyx/batch-size 1}
            :lifecycles []}
   :schema {:task-map   os/TaskMap
            :lifecycles [os/Lifecycle]}})

(defn configure-job
      [job _opts]
      (-> job
          (add-task
            (kafka/input-task
              :read-signal-config
              {:task-opts      (merge
                                 (taks-config/kafka-consumer)
                                 {:kafka/topic (kafka-topic :signal-config) :kafka/group-id "signal-config-consumer"})
               :lifecycle-opts {}}
              ))
          (add-task log)
          (add-task
            (rethink/output-task
              :save-signal-config
              {:task-opts      (onyx-defaults)
               :lifecycle-opts (merge (taks-config/rethink) {:rethinkdb/table "signal"})}))))


(defn build-job []
      (let [job (-> (build-base-job {})
                    (configure-job {}))]
           (log/debug "building job: signal-config")
           {:name :signal/config
            :job  job}))