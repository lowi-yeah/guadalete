;//              _   _     _         __ _          _        _    _
;//   _ __  __ _| |_| |_  | |____ _ / _| |____ _  | |__ _ _(_)__| |__ _ ___
;//  | '  \/ _` |  _|  _| | / / _` |  _| / / _` | | '_ \ '_| / _` / _` / -_)
;//  |_|_|_\__, |\__|\__| |_\_\__,_|_| |_\_\__,_| |_.__/_| |_\__,_\__, \___|
;//           |_|                                                 |___/

(ns guadalete.systems.mqtt-kafka-bridge
    ;(:use
    ;  [clj-kafka.core :only (with-resource to-clojure)])
    (:import
      [kafka.serializer StringDecoder])
    (:require
      [clojure.string :as str]
      [clojure.core.async :refer [>! >!! <! go go-loop chan alts! close!]]
      [cheshire.core :refer [generate-string parse-string]]
      [com.stuartsierra.component :as component]
      [clj-kafka.admin :as admin]
      [clojurewerkz.machine-head.client :as mh]
      [clj-kafka.new.producer :refer [producer send record string-serializer byte-array-serializer]]
      [clj-kafka.core :refer [to-clojure]]
      [clj-kafka.consumer.zk :as zk]
      [taoensso.timbre :as log]
      [guadalete.config.kafka :as kafka]
      [guadalete.utils.util :refer [now]]
      [clojure.stacktrace :refer [print-stack-trace]]))


(defn- mqtt->kafka [mqtt-topic mqtt-payload]
       (let [[signal-type message-type id] (str/split mqtt-topic #"/")
             kafka-topic (str kafka/prefix signal-type "-" message-type)
             data (parse-string (String. mqtt-payload "UTF-8"))
             message (condp = kafka-topic
                            "gdlt-sgnl-v" {:data data
                                           :id   id
                                           :at   (now)}
                            (-> data
                                (assoc :id id)
                                (assoc :at (now))))]
            {:topic   kafka-topic
             :key     id
             :message message}))

(defn- kafka->mqtt [kafka-topic kafka-payload]
       (log/debug "kafka->mqtt" kafka-topic kafka-payload)
       "kafka->mqtt")

(defn- publish! [message-and-metadata]
       (let [
             ;payload
             ]
            ;(log/debug "publish" message-and-metadata)

            ))

(defn- dispatch! [kafka-producer ^String mqtt-topic _ ^bytes mqtt-payload]
       (let [{:keys [topic key message]} (mqtt->kafka mqtt-topic mqtt-payload)
             r (record topic key (-> message (generate-string)))]
            ;(log/debug r)
            (send kafka-producer r)))


(defn- deserialize-message [{:keys [topic offset partition key value]}]
       (let [
             ;key* (.getBytes key "UTF-8")
             ;value* (.getBytes value "UTF-8")
             ]
            ;(log/debug "deserialize-message" topic key* value*)
            (log/debug "deserialize-message" topic key value)

            {:topic topic
             :id    "some-id"}))

(def xform (comp (map deserialize-message)))

(defrecord MqttKafkaBridge [kafka mqtt]
           component/Lifecycle
           (start [component]
                  (log/info "Starting component: mqtt-kafka-bridge")
                  (log/info "kafka" kafka)
                  (log/info "mqtt topics" (:topics mqtt))
                  (if (:brokers kafka)

                    (try


                      (let [p-config {"bootstrap.servers" (:brokers kafka)
                                      "acks"              "0"
                                      "batch.size"        "1"
                                      "linger.ms"         "0"}
                            p (producer p-config (string-serializer) (string-serializer))

                            c-config {"zookeeper.connect"       (:zookeeper-address kafka)
                                      "group.id"                "kafka-mqtt.consumer"
                                      "auto.offset.reset"       "largest"
                                      "auto.commit.interval.ms" "10000"
                                      "auto.commit.enable"      "true"}

                            c (zk/consumer c-config)
                            ;kafka-stream (zk/create-message-stream c "gdlt-lght-o")
                            ;message-opts {:key-decoder   (StringDecoder. nil)
                            ;              :value-decoder (StringDecoder. nil)}

                            messages (zk/stream-seq (zk/create-message-stream c "gdlt-lght-o" (StringDecoder. nil) (StringDecoder. nil)))
                            ]

                           (mh/subscribe (:conn mqtt) (:topics mqtt) (partial dispatch! p))

                           (go (run! publish! (eduction xform messages)))

                           (-> component
                               (assoc :consumer c))
                           )
                      (catch Exception e
                        (log/error "ERROR in MqttKafkaBridge" e)
                        (print-stack-trace e)
                        component
                        ))
                    (do
                      (log/warn "MQTT->Kafka brigde not started, as no kafka brokers could be found. bummer…")
                      component)))

           (stop [component]
                 (log/info "Stopping component: mqtt-kafka-bridge")
                 (if (not (nil? (:consumer component)))
                   (zk/shutdown (:consumer component)))
                 (dissoc component :consumer)))

(defn mqtt-kafka-bridge []
      (map->MqttKafkaBridge {}))