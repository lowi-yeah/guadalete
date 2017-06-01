(ns guadalete.systems.core
    (:require
      [com.stuartsierra.component :as component]
      (system.components [repl-server :refer [new-repl-server]])
      [taoensso.timbre :as log]
      ;[guadalete.systems.onyx :refer [onyx]]
      [guadalete.utils.util :refer [load-config load-zeroconfig load-static-config pretty]]
      [guadalete.config.zeroconf :as zeroconf]
      ;[guadalete.systems.kafka :refer [kafka]]
      [guadalete.systems.mqtt :refer [mqtt]]
      [guadalete.systems.mongodb :refer [mongodb]]
      [guadalete.systems.debug :refer [debug]]

      ;[guadalete.systems.async :refer [new-async]]
      ;[guadalete.systems.kafka-consumer.core :refer [new-kafka-consumer]]
      ;[guadalete.systems.kafka-async :refer [new-kafka-async-pipe]]
      ;[guadalete.systems.onyx-jobs :refer [job-runner]]
      ;[guadalete.systems.zookeeper :refer [zookeeper]]
      ;[guadalete.systems.bookkeeper :refer [multi-bookie-server]]
      ;[guadalete.onyx.plugin.mqtt]
      ))

(defn dev-system
      "Assembles and returns components for a base application"
      []
      (log/info "starting development system")
      (let [
            config (load-static-config)
            ;config (load-zeroconfig)
            ]
           (component/system-map
             :mqtt (mqtt (:mqtt config))
             :mongodb (mongodb(:mongodb config))
             :debug (component/using (debug) [:mongodb])
             ;:zookeeper (zookeeper (:zookeeper config))
             ;:kafka (kafka (:kafka config))
             ;:bookkeeper (component/using (multi-bookie-server (:onyx config)) [:zookeeper])
             ;:onyx (onyx (:onyx config))
             ;:job-runner (component/using (job-runner) [:onyx :rethinkdb])
             )))


(defn prod-system
      "Assembles and returns components for a base application"
      []
      (log/info "starting production system")
      (let [config (load-config)]
           (log/debug "configuration:" config)
           (component/system-map
             ;:rethinkdb (rethinkdb (:rethinkdb config))
             ;:zookeeper (zookeeper (:onyx config))
             ;:bookkeeper (component/using (multi-bookie-server (:onyx config)) [:zookeeper])
             ;:kafka (kafka (:kafka config))
             ;:mqtt (mqtt (:mqtt config))
             ;:onyx (onyx (:onyx config))
             ;:job-runner (component/using (job-runner) [:onyx :rethinkdb])
             )))
