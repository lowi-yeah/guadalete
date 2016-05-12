;//              _   _                                        _
;//   _ __  __ _| |_| |_   __ ___ _ __  _ __ ___ _ _  ___ _ _| |_
;//  | '  \/ _` |  _|  _| / _/ _ \ '  \| '_ \ _ \ ' \/ -_) ' \  _|
;//  |_|_|_\__, |\__|\__| \__\___/_|_|_| .__\___/_||_\___|_||_\__|
;//           |_|                      |_|

(ns guadalete.systems.mqtt.core
    (:require
      [com.stuartsierra.component :as component]
      [clj-kafka.admin :as admin]
      [onyx.api]
      [clojurewerkz.machine-head.client :as mh]
      [taoensso.timbre :as log]
      ))


(defrecord Mqtt [mqtt-broker mqtt-id mqtt-topics]
           component/Lifecycle
           (start [component]
                  (log/info "Starting component: mqtt")
                  (log/debug "\t mqtt-broker:" mqtt-broker)
                  (log/debug "\t mqtt-id:" mqtt-id)

                  (let [conn (mh/connect mqtt-broker mqtt-id)]
                       ;(log/debug "\t connection" conn)
                       (assoc component :conn conn :topics mqtt-topics)))

           (stop [component]
                 (log/info "Stopping component: mqtt")
                 (let [conn (:conn component)]
                      (if (and conn (mh/connected? conn)) (mh/disconnect conn))
                      (dissoc component :conn :topics))))

(defn new-mqtt [config]
      (map->Mqtt config))