;//              _   _     _         __ _          _        _    _
;//   _ __  __ _| |_| |_  | |____ _ / _| |____ _  | |__ _ _(_)__| |__ _ ___
;//  | '  \/ _` |  _|  _| | / / _` |  _| / / _` | | '_ \ '_| / _` / _` / -_)
;//  |_|_|_\__, |\__|\__| |_\_\__,_|_| |_\_\__,_| |_.__/_| |_\__,_\__, \___|
;//           |_|                                                 |___/

(ns guadalete.systems.mqtt-kafka-bridge
    (:require
      [clojure.string :as str]
      [clojure.core.async :refer [>! >!! <! go-loop chan alts! close!]]
      [cheshire.core :refer [generate-string parse-string]]
      [com.stuartsierra.component :as component]
      [clj-kafka.admin :as admin]
      [clojurewerkz.machine-head.client :as mh]
      [clj-kafka.new.producer :refer [producer send record string-serializer byte-array-serializer]]
      [taoensso.timbre :as log]
      [guadalete.config.kafka :as kafka]
      [guadalete.utils.util :refer [now]]))


(defn- mqtt->kafka [mqtt-topic mqtt-payload]
       (let [[signal-type message-type id] (str/split mqtt-topic #"/")
             message* (parse-string (String. mqtt-payload "UTF-8"))
             kafka-topic (str kafka/prefix signal-type "-" message-type)]
            {:topic   kafka-topic
             :key     id
             :message {:data message*
                       :id   id
                       :at   (now)}}))

(defn- dispatch! [kafka-producer ^String mqtt-topic _ ^bytes mqtt-payload]
       (let [{:keys [topic key message]} (mqtt->kafka mqtt-topic mqtt-payload)
             r (record topic key (-> message (generate-string)))]
            (send kafka-producer r)))

(defrecord MqttKafkaBridge [kafka mqtt]
           component/Lifecycle
           (start [component]
                  (log/info "Starting component: mqtt-kafka-bridge")
                  (log/info "kafka" kafka)
                  (log/info "mqtt topics" (:topics mqtt))
                  (let [p-config {"bootstrap.servers" (:brokers kafka)
                                  "acks"              "0"
                                  "batch.size"        "1"
                                  "linger.ms"         "0"
                                  ;"max.request.size"  "100"
                                  }
                        p (producer p-config (string-serializer) (string-serializer))
                        ;p (producer p-config (byte-array-serializer) (byte-array-serializer))
                        ]
                       (mh/subscribe (:conn mqtt) (:topics mqtt) (partial dispatch! p))
                       component))

           (stop [component]
                 (log/info "Stopping component: mqtt-kafka-bridge")
                 component))

(defn new-mqtt-kafka-bridge []
      (map->MqttKafkaBridge {}))