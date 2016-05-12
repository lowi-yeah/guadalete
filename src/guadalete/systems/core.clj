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
      [guadalete.systems.kafka-signals :refer [kafka-signals]]
      [guadalete.systems.rethinkdb.core :refer [new-rethinkdb]]
      [guadalete.jobs.signal-configuration.component :refer [signal-configuration-job]]
      [guadalete.signals.sine :refer [new-sine-signal]]
      [guadalete.signals.weather :refer [new-weather-signal]]
      ))

(defsystem dev-system
           [:onyx (new-onyx (config/onyx))
            :kafka (new-kafka (config/kafka))
            :mqtt (new-mqtt (config/mqtt))
            :rethinkdb (new-rethinkdb (config/rethinkdb))
            :mqtt-kafkabridge (component/using (new-mqtt-kafka-bridge (config/kafka)) [:kafka :mqtt])
            :sine-signal (component/using (new-sine-signal (merge (config/mqtt) {:mqtt-id "sin3" :increment 0.01 :interval 200})) [:kafka :mqtt :mqtt-kafkabridge])
            :temperature-signal (component/using (new-weather-signal (merge (config/weather-signal) {:mqtt-id "weather"})) [:kafka :mqtt :mqtt-kafkabridge])
            ;:kafka-signals (component/using (kafka-signals) [:onyx])
            :signal-configuration-job (component/using (signal-configuration-job (config/signal-configuration)) [:onyx])
            ])

(defsystem prod-system
           [:onyx (new-onyx (config/onyx))
            ;:capture-kafka (component/using (new-capture) [:onyx])
            :repl-server (new-repl-server (Integer. (env :repl-port)))])