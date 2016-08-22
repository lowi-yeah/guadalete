(ns guadalete.utils.redis-time-series
    (:require
      [taoensso.timbre :as log]
      [cheshire.core :as json]
      [clj-uuid :as uuid]))

(defn now [] (System/currentTimeMillis))

(defn normalize-time
      [timestep t]
      (- t (mod t timestep)))

(defn get-key [prefix timestep t]
      (str prefix ":" (normalize-time timestep t)))

(defn tsencode
      "Encode datapoint as Base64 (if required)."
      [data]
      ;todo: implement me!
      )

(defn tsdecode
      "Deccode datapoint from Base64 (if required)."
      [data]
      ;todo: implement me!
      )

(defn add
      "Add a datapoint"
      [data]
      (let [t (now)
            key (get-key "signal" 1000 t)
            value (str t (byte 0x01) data (byte 0x00))]
           [key value]))

(defn record-event
      "Create the entries for an event.
      @see: https://www.infoq.com/articles/redis-time-series"
      [prefix data]
      (let [event-id (uuid/v1)
            event-key (str prefix ":" event-id)
            data* (assoc data :ev-id (str event-id))]
           [{:op :multi :args []}
            {:op :hmset :args [event-key data*]}
            ;{:op :zadd :args [(str prefix "-events") (uuid/get-timestamp event-id)]}
            {:op :exec :args []}]))