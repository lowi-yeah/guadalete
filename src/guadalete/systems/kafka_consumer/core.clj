(ns guadalete.systems.kafka-consumer.core
    (:require
      [clojure.core.async :as async :refer [chan >! >!! <! go go-loop thread close! alts!]]
      [com.stuartsierra.component :as component]
      [taoensso.timbre :as log]
      [clj-kafka.admin :as admin]
      [clj-kafka.consumer.zk :as zk]
      [clj-kafka.core :as kafka]
      [clj-kafka.zk :refer [broker-list brokers topics partitions]]
      [clj-kafka.offset :as offset :refer [fetch-consumer-offsets]]
      [guadalete.utils.util :refer [in? uuid]]

      ))

;; hypothetical transformation
;(def xform (comp (map deserialize-message)
;                 (filter production-traffic)
;                 (map parse-user-agent-string)))

(def silly-xform (comp (map #(identity %))
                       (map #(identity %))))

(defn- log-it [x]
       (log/debug "logging it:" (str x)))

(defn- message-to-vec
       "returns a vector of all of the message fields"
       [^kafka.message.MessageAndMetadata message]
       [(.topic message) (.offset message) (.partition message) (.key message) (.message message)])


(defn- default-iterator
       "processing all streams in a thread and printing the message field for each message"
       [stream]
       (log/debug "default iterator." (str stream))
       (let [c (chan)
             uuid (uuid)]
            (go
              (doseq
                [^kafka.message.MessageAndMetadata message stream]
                (>! c (String. (nth (message-to-vec message) 4)))))
            c))

(defrecord KafkaConsumer [config topikz]
           component/Lifecycle
           (start [component]
                  (log/info "**************** Starting KafkaConsumer ***************")
                  (log/debug "***** config" config)
                  (log/debug "***** topikz" topikz)
                  (let [c (zk/consumer config)
                        t (->>
                            (topics config)
                            (filter #(in? topikz %))
                            (map (fn [t] [t (partitions config t)]))
                            (into {}))
                        stream (zk/create-message-stream c "gdlt-sgnl-v")
                        stream-channel (default-iterator stream)
                        stop-channel (chan)
                        ;offets (fetch-consumer-offsets "127.0.0.1:9092" {"zookeeper.connect" "127.0.0.1:2181"} "gdlt-sgnl-v" "guadalete-ui.kafka-consumer")
                        ]
                       (go-loop
                         []
                         (let [[msg ch] (alts! [stream-channel stop-channel])]
                              (when-not (= ch stop-channel)
                                        (log/debug (str msg))
                                        (recur))))
                       (assoc component :consumer c :stop-channel stop-channel)))

           (stop [component]
                 (log/info "Stopping KafkaConsumer")
                 (zk/shutdown (:consumer component))
                 (>!! (:stop-channel component) :halt)
                 (dissoc component :consumer)))

(defn new-kafka-consumer [config]
      (log/debug "new-kafka-consumer" config)
      (map->KafkaConsumer config))