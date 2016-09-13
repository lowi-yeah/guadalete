(ns guadalete.systems.core
    (:require
      [system.core :refer [defsystem]]
      [com.stuartsierra.component :as component]
      (system.components
        [repl-server :refer [new-repl-server]])
      [environ.core :refer [env]]
      [taoensso.timbre :as log]
      [onyx.messaging.aeron :refer [aeron-messenger]]

      [guadalete.systems.onyx :refer [onyx]]
      [guadalete.systems.kafka :refer [new-kafka]]
      [guadalete.systems.mqtt.core :refer [new-mqtt]]
      [guadalete.systems.async :refer [new-async]]
      [guadalete.systems.mqtt-kafka-bridge :refer [mqtt-kafka-bridge]]
      [guadalete.systems.rethinkdb.core :refer [new-rethinkdb]]
      [guadalete.systems.kafka-consumer.core :refer [new-kafka-consumer]]
      [guadalete.systems.kafka-async :refer [new-kafka-async-pipe]]
      [guadalete.systems.onyx-jobs :refer [job-runner]]
      [guadalete.systems.zookeeper :refer [zookeeper]]
      [guadalete.systems.bookkeeper :refer [multi-bookie-server]]
      [guadalete.config
       [core :as config]
       [kafka :as kafka-config]]
      ))


(defsystem dev-system [
                       ;//   _       __             _               _
                       ;//  (_)_ _  / _|_ _ __ _ ___ |_ _ _ _  _ __| |_ _  _ _ _ ___
                       ;//  | | ' \|  _| '_/ _` (_-<  _| '_| || / _|  _| || | '_/ -_)
                       ;//  |_|_||_|_| |_| \__,_/__/\__|_|  \_,_\__|\__|\_,_|_| \___|
                       ; these are the base systems required for running guadalete.
                       :rethinkdb (new-rethinkdb (config/rethinkdb))
                       :zookeeper (zookeeper (:env-config (config/onyx)))
                       :bookkeeper (component/using (multi-bookie-server (:env-config (config/onyx))) [:zookeeper])
                       :kafka (new-kafka (config/kafka))
                       :mqtt (new-mqtt (config/mqtt))
                       :mqtt-kafkabridge (component/using (mqtt-kafka-bridge) [:kafka :mqtt])
                       :onyx (onyx (config/onyx))

                       ;//     _     _
                       ;//    (_)___| |__ ___
                       ;//    | / _ \ '_ (_-<
                       ;//   _/ \___/_.__/__/
                       ;//  |__/
                       ; the jobs run by onyx
                       :job-runner (component/using (job-runner) [:onyx :kafka :mqtt :rethinkdb])
                       ;:job-runner (component/using (job-runner) [:rethinkdb])
                       ])

(defsystem prod-system
           [:onyx (onyx (config/onyx))
            ;:capture-kafka (component/using (new-capture) [:onyx])
            :repl-server (new-repl-server (Integer. (env :repl-port)))])