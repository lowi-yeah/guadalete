(ns guadalete.config.mqtt
    (:require
      [guadalete.config.environment :as env]
      [taoensso.timbre :as log]))


(defn config* []
      {:mqtt-broker (env/get-value :mqtt/broker)
       :mqtt-id     (env/get-value :mqtt/id)
       :mqtt-topics (env/get-value :mqtt/topics)})

