;//              _   _     _         __ _          _        _    _
;//   _ __  __ _| |_| |_  | |____ _ / _| |____ _  | |__ _ _(_)__| |__ _ ___
;//  | '  \/ _` |  _|  _| | / / _` |  _| / / _` | | '_ \ '_| / _` / _` / -_)
;//  |_|_|_\__, |\__|\__| |_\_\__,_|_| |_\_\__,_| |_.__/_| |_\__,_\__, \___|
;//           |_|                                                 |___/

(ns guadalete.systems.mqtt-kafka-bridge.core
    (:require
      [clojure.string :as str]
      [clojure.core.async :refer [>! >!! <! go-loop chan alts! close!]]
      [cheshire.core :refer [generate-string parse-string]]
      [com.stuartsierra.component :as component]
      [clj-kafka.admin :as admin]
      [clojurewerkz.machine-head.client :as mh]
      [clj-kafka.new.producer :refer [producer send record string-serializer byte-array-serializer]]
      [taoensso.timbre :as log]
      [guadalete.utils.config :refer [kafka-prefix]]
      ))

(defn- mqtt->kafka [mqtt-topic mqtt-payload]
       (let [[signal-type message-type id] (str/split mqtt-topic #"/")
             message* (parse-string (String. mqtt-payload "UTF-8") true)
             kafka-topic (str kafka-prefix signal-type "-" message-type)]
            {:topic   kafka-topic
             :message {:id id :data message*}}))

(defn- dispatch! [kafka-producer ^String mqtt-topic _ ^bytes mqtt-payload]

       (let [kafka-map (mqtt->kafka mqtt-topic mqtt-payload)]
            ;(log/debug "mqtt->kafka!" mqtt-topic (:topic kafka-map) (generate-string (:message kafka-map)))
            (send kafka-producer (record (:topic kafka-map) (generate-string (:message kafka-map))))))

(defrecord MqttKafkaBridge [kafka mqtt]
           component/Lifecycle
           (start [component]
                  (log/info "Starting component: mqtt-kafka-bridge" (:topics mqtt))
                  (let [p (producer {"bootstrap.servers" (:brokers kafka)} (string-serializer) (string-serializer))]
                       (mh/subscribe (:conn mqtt) (:topics mqtt) (partial dispatch! p))
                       component))

           (stop [component]
                 (log/info "Stopping component: mqtt-kafka-bridge")
                 component))

(defn new-mqtt-kafka-bridge []
      (map->MqttKafkaBridge {}))