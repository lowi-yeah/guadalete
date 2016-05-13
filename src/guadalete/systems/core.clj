(ns guadalete.systems.core
    (:require [system.core :refer [defsystem]]
      [com.stuartsierra.component :as component]
      (system.components
        [repl-server :refer [new-repl-server]])
      [environ.core :refer [env]]
      [onyx.messaging.aeron :refer [aeron-messenger]]
      [clojurewerkz.machine-head.client :as mh]
      [guadalete.utils.config :as config]

      [guadalete.systems.onyx.core :refer [new-onyx]]
      [guadalete.systems.kafka.core :refer [new-kafka]]
      [guadalete.systems.mqtt.core :refer [new-mqtt]]
      [guadalete.systems.mqtt-kafka-bridge.core :refer [new-mqtt-kafka-bridge]]

      [guadalete.systems.kafka-signals :refer [raw-kafka-signals]]
      [guadalete.systems.rethinkdb.core :refer [new-rethinkdb]]
      [guadalete.jobs.signal.raw.value.component :refer [signal-raw-values]]
      [guadalete.jobs.signal.raw.config.component :refer [signal-raw-config]]
      [guadalete.signals.sine :refer [new-sine-signal]]
      [guadalete.signals.weather :refer [new-weather-signal]]
      [guadalete.signals.mock-switch :refer [new-mock-switch]]
      ))

(defsystem dev-system [
                       ;//   _       __             _               _
                       ;//  (_)_ _  / _|_ _ __ _ ___ |_ _ _ _  _ __| |_ _  _ _ _ ___
                       ;//  | | ' \|  _| '_/ _` (_-<  _| '_| || / _|  _| || | '_/ -_)
                       ;//  |_|_||_|_| |_| \__,_/__/\__|_|  \_,_\__|\__|\_,_|_| \___|
                       ; these are the base systems required for running guadalete.
                       :onyx (new-onyx (config/onyx))
                       :kafka (new-kafka (config/kafka))
                       :mqtt (new-mqtt (config/mqtt))
                       :rethinkdb (new-rethinkdb (config/rethinkdb))
                       :mqtt-kafkabridge (component/using (new-mqtt-kafka-bridge) [:kafka :mqtt])

                       ;//      _                _
                       ;//   ____)__ _ _ _  __ _| |___
                       ;//  (_-< / _` | ' \/ _` | (_-<
                       ;//  /__/_\__, |_||_\__,_|_/__/
                       ;//       |___/
                       ; some signals used for development/testing
                       :sine-signal (component/using (new-sine-signal (merge (config/mqtt) {:mqtt-id "sin3" :increment 0.01 :interval 2000})) [:kafka :mqtt :mqtt-kafkabridge])
                       :weather-signals (component/using (new-weather-signal (merge (config/weather-signal) {:mqtt-id "weather"})) [:kafka :mqtt :mqtt-kafkabridge])
                       :mock-switch (component/using (new-mock-switch (merge (config/mqtt) {:mqtt-id "mock-switch"})) [:kafka :mqtt :mqtt-kafkabridge])

                       ;//     _     _
                       ;//    (_)___| |__ ___
                       ;//    | / _ \ '_ (_-<
                       ;//   _/ \___/_.__/__/
                       ;//  |__/
                       ; the jobs run by onyx
                       ; each job has its own component which can start & stop it.
                       ; whether or not this is a good idea (architecture-wise) remains to be seen…

                       ; processes signal configuration messages coming in via mqtt ( topic: sgnl/c/:id )
                       ;
                       :signal-raw-config (component/using (signal-raw-config (config/signal-configuration)) [:onyx])

                       ; processes signal value messages coming in via mqtt ( topic: sgnl/v/:id )
                       :signal-raw-values (component/using (signal-raw-values (config/signal-value)) [:onyx])
                       ])

(defsystem prod-system
           [:onyx (new-onyx (config/onyx))
            ;:capture-kafka (component/using (new-capture) [:onyx])
            :repl-server (new-repl-server (Integer. (env :repl-port)))])