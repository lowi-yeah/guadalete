(ns guadalete.systems.core
    (:require [system.core :refer [defsystem]]
      [com.stuartsierra.component :as component]
      (system.components
        [repl-server :refer [new-repl-server]])
      [environ.core :refer [env]]
      [onyx.messaging.aeron :refer [aeron-messenger]]
      [clojurewerkz.machine-head.client :as mh]
      [guadalete.systems.onyx.core :refer [new-onyx]]
      [guadalete.systems.kafka :refer [new-kafka]]
      [guadalete.systems.mqtt.core :refer [new-mqtt]]
      [guadalete.systems.mqtt-kafka-bridge :refer [new-mqtt-kafka-bridge]]
      [guadalete.systems.rethinkdb.core :refer [new-rethinkdb]]
      [guadalete.systems.kafka-consumer.core :refer [new-kafka-consumer]]
      [guadalete.systems.kafka-async :refer [new-kafka-async-pipe]]
      [guadalete.systems.channel-listener :refer [new-channel-listener]]
      [guadalete.jobs.component :refer [job-runner]]
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


                       :kafka (new-kafka (config/kafka))
                       :mqtt (new-mqtt (config/mqtt))
                       :rethinkdb (new-rethinkdb (config/rethinkdb))
                       :mqtt-kafkabridge (component/using (new-mqtt-kafka-bridge) [:kafka :mqtt])

                       ;:kafka-async (component/using
                       ;               (new-kafka-async-pipe {:config (kafka-config/consumer) :topik "gdlt-sgnl-v"})
                       ;               [:kafka])



                       ;:onyx (new-onyx (config/onyx))

                       ;//     _     _
                       ;//    (_)___| |__ ___
                       ;//    | / _ \ '_ (_-<
                       ;//   _/ \___/_.__/__/
                       ;//  |__/
                       ; the jobs run by onyx
                       ;:job-runner (component/using (job-runner) [:onyx :kafka :mqtt :rethinkdb])

                       ;//      _     _
                       ;//   __| |___| |__ _  _ __ _
                       ;//  / _` / -_) '_ \ || / _` |
                       ;//  \__,_\___|_.__/\_,_\__, |
                       ;//                     |___/
                       :kafka-async (component/using
                                      (new-kafka-async-pipe {:config (kafka-config/consumer) :topik "gdlt-sgnl-v"})
                                      [:kafka])
                       :kafka-consumer (new-kafka-consumer (kafka-config/consumer))
                       ;:channel-listener (new-channel-listener)
                       ])

(defsystem prod-system
           [:onyx (new-onyx (config/onyx))
            ;:capture-kafka (component/using (new-capture) [:onyx])
            :repl-server (new-repl-server (Integer. (env :repl-port)))])