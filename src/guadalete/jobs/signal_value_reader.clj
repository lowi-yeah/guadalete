(ns guadalete.jobs.signal-value-reader
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
       [redis :as redis]]
      [guadalete.jobs.state :as state]))

(def base-job
  {:workflow       [[:subscribe :log]
                    [:log :write]]
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

(defn configure-job
      [job]
      (let [job* (-> job
                     (add-task
                       (async/subscribe-task
                         :subscribe
                         {:task-opts      (onyx-defaults)
                          :lifecycle-opts {:signal/id "slowsine"}}))

                     (add-task (log-task :log))

                     (add-task
                       (async/output-task
                         :write
                         {:task-opts (onyx-defaults)}))
                     )]
           job*))

(defn build-job []
      {:name :signal/value-reader
       :job  (configure-job base-job)})