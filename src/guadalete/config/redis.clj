(ns guadalete.config.redis
    (:require
      [guadalete.config.environment :as env]
      [taoensso.timbre :as log]))

(defn config* []
      {:redis/uri             (env/get-value :redis/uri)
       :redis/read-timeout-ms (env/get-value :redis/read-timeout-ms)})