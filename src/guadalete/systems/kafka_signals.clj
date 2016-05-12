(ns guadalete.systems.kafka-signals
    (:require
      [clojure.core.async :refer [>! >!! <! go-loop chan alts! close!]]
      [com.stuartsierra.component :as component]
      [clj-kafka.offset :as offset]
      [onyx.api]
      [taoensso.timbre :as log]
      [onyx.plugin.core-async :refer [take-segments!]]
      [lib-onyx.log-subscriber :refer [start-log-subscriber stop-log-subscriber]]
      [guadalete.jobs.signal-job :refer [build-job]]
      [guadalete.tasks.core-async :refer [get-core-async-channels]]
      [guadalete.utils.lifecycle :refer [collect-outputs!]]))

(defn- channel-loop [kafka-channel stop-channel subscriber]
       (go-loop []
                (let [[message ch] (alts! [kafka-channel stop-channel])]
                     (when-not (= ch stop-channel)
                               (log/debug message)
                               (recur)))))

(defrecord KafkaSignals [onyx]
           component/Lifecycle
           (start [component]
                  (log/info "**** start KafkaSignals")
                  (let [stop-channel (chan)
                        peer-config (:peer-config onyx)
                        job (build-job :dev)
                        subscriber (start-log-subscriber peer-config)
                        {:keys [write-messages] :as async-channels} (get-core-async-channels job)
                        {:keys [job-id] :as job-reciept} (onyx.api/submit-job peer-config job)]
                       (channel-loop write-messages stop-channel subscriber)
                       (assoc component :stop-channel stop-channel :subscriber subscriber)))
           (stop [component]
                 (log/info "**** stop KafkaSignals")
                 (>!! (:stop-channel component) "halt!")
                 (stop-log-subscriber (:subscriber component))
                 (close! (:stop-channel component))
                 component))

(defn kafka-signals []
      (map->KafkaSignals {}))