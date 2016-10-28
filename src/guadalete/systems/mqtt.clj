;//              _   _                                        _
;//   _ __  __ _| |_| |_   __ ___ _ __  _ __ ___ _ _  ___ _ _| |_
;//  | '  \/ _` |  _|  _| / _/ _ \ '  \| '_ \ _ \ ' \/ -_) ' \  _|
;//  |_|_|_\__, |\__|\__| \__\___/_|_|_| .__\___/_||_\___|_||_\__|
;//           |_|                      |_|

(ns guadalete.systems.mqtt
    (:require
      [com.stuartsierra.component :as component]
      [clj-kafka.admin :as admin]
      [onyx.api]
      [clojurewerkz.machine-head.client :as mh]
      [taoensso.timbre :as log]))


(defrecord Mqtt [broker id topics]
           component/Lifecycle
           (start [component]
                  (log/info "Starting component: mqtt")
                  (log/debug "\t mqtt-broker:" broker)
                  (log/debug "\t mqtt-id:" id)
                  (log/debug "\t mqtt-topics:" topics)

                  (let [conn (mh/connect broker id)]
                       (assoc component :conn conn :topics topics)))

           (stop [component]
                 (log/info "Stopping component: mqtt")
                 (let [conn (:conn component)]
                      (if (and conn (mh/connected? conn)) (mh/disconnect conn))
                      (dissoc component :conn :topics))))

(defn mqtt [config]
      (map->Mqtt config))