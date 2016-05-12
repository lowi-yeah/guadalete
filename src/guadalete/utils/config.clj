(ns guadalete.utils.config
    (:require [environ.core :refer [env]]))

(defn- get*
       [key] (get env key))

(defn- get-bool
       [key] (Boolean/valueOf (get env key)))

(defn onyx []
      {
       :n-peers     (get* :onyx.peer/n-peers)
       :peer-config {:zookeeper/address                     (get* :zookeeper/address)
                     :onyx.messaging/impl                   (get* :onyx.messaging/impl)
                     :onyx.peer/job-scheduler               (get* :onyx.peer/job-scheduler)
                     :onyx.messaging/peer-port              (get* :onyx.messaging/peer-port)
                     :onyx.messaging/bind-addr              (get* :onyx.messaging/bind-addr)
                     :onyx.log/config                       (get* :onyx.log/config)
                     :onyx.messaging.aeron/embedded-driver? (get-bool :onyx.messaging.aeron/embedded-driver?)}})

(defn kafka []
      {:zookeeper-address (get* :zookeeper/address)
       :kafka-topics      (get* :kafka/topics)})

(defn rethinkdb []
      {:host     (get* :rethinkdb/host)
       :port     (get* :rethinkdb/port)
       :auth-key (get* :rethinkdb/auth-key)
       :db       (get* :rethinkdb/db)
       :tables   (get* :rethinkdb/tables)})



(defn mqtt []
      {:mqtt-broker (get* :mqtt/broker)
       :mqtt-id     (get* :mqtt/id)
       :mqtt-topics (get* :mqtt/topics)})

(defn signal-configuration []
      (merge (rethinkdb) {:table "signal"}))


(defn weather-signal []
      (merge (mqtt) {
                     ;:value-update-interval  120000        ; two minutes
                     :value-update-interval  (* 4 1000)
                     :config-update-interval (* 4 1000)
                     :parameters             ["temperature" "windSpeed" "cloudCover" "pressure" "ozone" "humidity"]
                     :config-map             {:type "analog"}
                     }))


