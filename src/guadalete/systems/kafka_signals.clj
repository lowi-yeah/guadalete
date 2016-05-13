(ns guadalete.systems.kafka-signals
    (:require
      [clojure.core.async :refer [>! >!! <! go-loop chan alts! close!]]
      [com.stuartsierra.component :as component]
      [clj-kafka.offset :as offset]
      [onyx.api]
      [taoensso.timbre :as log]
      [lib-onyx.log-subscriber :refer [start-log-subscriber stop-log-subscriber]]
      [guadalete.jobs.signal-job :refer [build-job]]
      [guadalete.tasks.core-async :refer [get-core-async-channels]]
      [guadalete.utils.lifecycle :refer [collect-outputs!]]))

(defn- channel-loop [kafka-channel stop-channel]
       (go-loop []
                (let [[message ch] (alts! [kafka-channel stop-channel])]
                     (when-not (= ch stop-channel)
                               (log/debug "RawKafkaSignals" message)
                               (recur)))))

(defrecord RawKafkaSignals [uri read-timeout-ms batch-size onyx]
           component/Lifecycle
           (start [component]
                  (log/info "**** start KafkaSignals")
                  (let [stop-channel (chan)
                        peer-config (:peer-config onyx)
                        redis-config {:redis/uri             uri
                                      :redis/read-timeout-ms read-timeout-ms
                                      :onyx/batch-size batch-size}
                        ;job (build-job :dev)
                        job (signal-value/build-job redis-config)
                        {:keys [write-messages] :as async-channels} (get-core-async-channels job)
                        {:keys [job-id] :as job-reciept} (onyx.api/submit-job peer-config job)]
                       (channel-loop write-messages stop-channel)
                       (assoc component :job-id job-id :stop-channel stop-channel :peer-config peer-config)))
           (stop [component]
                 (log/info "**** stop KafkaSignals")
                 (onyx.api/kill-job (:peer-config component) (:job-id component))
                 (>!! (:stop-channel component) "halt!")
                 (close! (:stop-channel component))
                 (dissoc component :job-id :stop-channel :peer-config)))

(defn raw-kafka-signals [config]
      (map->RawKafkaSignals config))