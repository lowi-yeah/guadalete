(ns guadalete.config.artnet
    (:require
      [guadalete.config.environment :as env]
      [taoensso.timbre :as log]))


(defn config* []
      {:node-port         7000
       :poll-interval     10000
       :broadcast-address "255.255.255.255"
       :server-port       6454})
