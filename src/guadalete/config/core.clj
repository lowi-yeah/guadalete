(ns guadalete.config.core
    (:require
      [guadalete.config.environment :as env]
      [taoensso.timbre :as log]
      [guadalete.config
       [mqtt :as mqtt]
       [kafka :as kafka]
       [rethinkdb :as rethinkdb]
       [redis :as redis]
       [onyx :as onyx]
       [artnet :as artnet]
       ]))


(defn mqtt [] (mqtt/config*))
(defn kafka [] (kafka/config*))
(defn rethinkdb [] (rethinkdb/config*))
(defn redis [] (redis/config*))
(defn onyx [] (onyx/config*))
(defn artnet [] (artnet/config*))
