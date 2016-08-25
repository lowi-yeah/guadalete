(ns guadalete.jobs.dev.signal-value-reader
    (:require
      [onyx.api]
      [onyx.plugin.kafka]
      [taoensso.carmine :as car]
      [taoensso.timbre :as log]
      [cheshire.core :as json]
      [schema.core :as s]
      [onyx.schema :as os]
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
       [redis :as redis-tasks]
       [items :as item-tasks]]
      [guadalete.jobs.state :as state]))

(def base-job
  {:workflow       [[:read :log]
                    [:log :write-to-async]]
   :lifecycles     []
   :catalog        []
   :task-scheduler :onyx.task-scheduler/balanced})


(defn log! [segment]
      (log/debug "log!" segment)
      segment)

(defn log-task
      [task-name]
      {:task   {:task-map   (merge
                              {:onyx/name task-name
                               :onyx/fn   ::log!
                               :onyx/type :function}
                              (onyx-defaults))
                :lifecycles []}
       :schema {:task-map   os/TaskMap
                :lifecycles [os/Lifecycle]}})

(defn configure-async-job
      [job signal-id]
      (let [job* (-> job

                     (add-task
                       (async/subscribe-task :read)
                       )

                     (add-task (log-task :log))

                     (add-task
                       (async/output-task
                         :write-to-async
                         {:task-opts (onyx-defaults)})))]
           job*))


(defn async-job [signal-id]
      {:name :signal/async-values
       :job  (configure-async-job base-job signal-id)})
