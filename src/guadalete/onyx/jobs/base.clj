(ns guadalete.onyx.jobs.base
    (:require
      [taoensso.timbre :as log]
      [guadalete.onyx.jobs.util :refer [empty-job add-tasks]]
      [guadalete.onyx.tasks.kafka :as kafka-tasks]
      [guadalete.onyx.tasks.redis :as redis-tasks]
      [guadalete.onyx.tasks.rethink :as rethink-tasks]
      [guadalete.onyx.tasks.identity :refer [log-task]]
      [guadalete.config.kafka :as kafka-config]
      [guadalete.config.task :as taks-config]
      [guadalete.config.onyx :refer [onyx-defaults]]
      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.schema.core :as gs]
      [guadalete.utils.util :refer [pretty validate!]]))

(defn signal-timeseries-consumer []
      (let [
            workflow [
                      ;[:read-from-kafka :write-to-redis]
                      [:read-from-kafka :log]
                      [:log :write-to-redis]
                      ]
            tasks [(kafka-tasks/signal-value-consumer :read-from-kafka "signal-value-consumer")
                   (log-task :log "signal/value")
                   (redis-tasks/write-signals-timeseries :write-to-redis)]
            job (-> empty-job
                    (add-tasks tasks)
                    (assoc :workflow workflow))]
           {:name :signal/value-logger
            :job  job}))

(s/defn ^:always-validate transform-signal-config :- gs/SignalConfig
        [segment :- gs/MqttSignalConfig]
        (let [segment* (-> segment
                           (assoc :name (get-in segment [:data :name]))
                           (assoc :type (get-in segment [:data :type]))
                           (assoc :at (read-string (:at segment)))
                           (dissoc :data))]
             (log/debug "transform-signal-config" segment*)
             segment*))

(defn signal-config-consumer []
      (let [
            workflow [[:read-from-kafka :transform-signal-config]
                      [:transform-signal-config :write-to-rethink]]
            tasks [
                   ;; kafka in
                   (kafka-tasks/signal-config-consumer :read-from-kafka)

                   ;; transform
                   {:task   {:task-map (merge (onyx-defaults)
                                              {:onyx/name   :transform-signal-config
                                               :onyx/plugin :onyx.peer.function/function
                                               :onyx/fn     ::transform-signal-config
                                               :onyx/type   :function
                                               :onyx/doc    "Transforms incoming signal configuration messages."})}
                    :schema {:task-map os/TaskMap}}

                   ;; rethinkDB out
                   (rethink-tasks/output :write-to-rethink {:task-opts (onyx-defaults) :lifecycle-opts (merge (taks-config/rethink) {:rethinkdb/table "signal"})})]

            job (-> empty-job
                    (add-tasks tasks)
                    (assoc :workflow workflow))]
           {:name :signal/config-handler
            :job  job}))

;//   _ _      _   _                  __ _
;//  | (_)__ _| |_| |_   __ ___ _ _  / _(_)__ _
;//  | | / _` | ' \  _| / _/ _ \ ' \|  _| / _` |
;//  |_|_\__, |_||_\__| \__\___/_||_|_| |_\__, |
;//      |___/                            |___/
(s/defn ^:always-validate transform-light-config :- gs/LightConfig
        [{:keys [name type] :as segment} :- gs/MqttLightConfig]
        (let [segment* (-> segment
                           (assoc :transport :mqtt)
                           (assoc :color-type (-> segment
                                                  (get-in [:data :color-type])
                                                  (keyword)))
                           (assoc :name (get-in segment [:data :name]))
                           (dissoc :at :data))]
             segment*))

(defn light-config-consumer []
      (let [
            workflow [[:read-from-kafka :light-config-transform]
                      [:light-config-transform :write-to-rethink]]
            tasks [(kafka-tasks/light-config-consumer :read-from-kafka)

                   {:task   {:task-map (merge {:onyx/name   :light-config-transform
                                               :onyx/plugin :onyx.peer.function/function
                                               :onyx/fn     ::transform-light-config
                                               :onyx/type   :function
                                               :onyx/doc    "Transforms incoming light configuration messages."}
                                              (onyx-defaults))}
                    :schema {:task-map os/TaskMap}}

                   (rethink-tasks/output :write-to-rethink {:task-opts (onyx-defaults) :lifecycle-opts (merge (taks-config/rethink) {:rethinkdb/table "light"})})]
            job (-> empty-job
                    (add-tasks tasks)
                    (assoc :workflow workflow))]
           {:name :light/config-handler
            :job  job}))

