(ns guadalete.systems.kafka-async
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


(def config* {"zookeeper.connect"  "127.0.0.1:2182"
              "group.id"           "clj-kafka.consumer"
              "auto.offset.reset"  "smallest"
              "auto.commit.enable" "false"})


(defrecord KafkaAsync [kafka config topik]
           component/Lifecycle
           (start [component]
                  (log/info "**************** Starting KafkaAsync ***************")
                  (log/debug "***** kafka" kafka)
                  (log/debug "***** config" config)
                  (log/debug "***** topik" topik)



                  (let [c (zk/consumer config*)
                        ;stream (zk/create-message-stream consumer topik)
                        ;stream-channel (default-iterator stream)
                        stop-channel (chan)
                        value-channel (chan)]


                       (take 2 (zk/messages c "gdlt-sgnl-v"))


                       ;(go-loop []
                       ;         (let [[msg ch] (alts! [stream-channel stop-channel])]
                       ;              (when-not (= ch stop-channel)
                       ;                        (log/debug (str msg))
                       ;                        ;(>! value-channel msg)
                       ;                        (recur))))
                       (assoc component :consumer c :stop-channel stop-channel :value-channel value-channel)
                       ))
           (stop [component]
                 (log/info "Stopping KafkaAsync")
                 (zk/shutdown (:consumer component))
                 ;(>!! (:stop-channel component) :halt)
                 (dissoc component :consumer :stop-channel :value-channel)))

(defn new-kafka-async-pipe [config]
      (map->KafkaAsync config))