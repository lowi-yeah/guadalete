(ns guadalete.utils.config
    (:require
      [environ.core :refer [env]]
      [taoensso.timbre :as log]))
;
;
;(defn signal-configuration []
;      (merge (rethinkdb) {:table "signal"}))
;
;(defn signal-value []
;      (merge (redis) {:batch-size 10}))
;
;(defn weather-signal []
;      (merge (mqtt) {
;                     ;:value-update-interval  120000        ; two minutes
;                     :value-update-interval  (* 60 1000)
;                     :config-update-interval (* 60 1000)
;                     :parameters             ["temperature" "windSpeed" "cloudCover" "pressure" "ozone" "humidity"]
;                     :config-map             {:type "analog"}
;                     }))
;
;(defn weather-signal []
;      (merge (mqtt) {
;                     ;:value-update-interval  120000        ; two minutes
;                     :value-update-interval  (* 10 1000)
;                     :config-update-interval (* 10 1000)
;                     :parameters             ["temperature" "windSpeed" "cloudCover" "pressure" "ozone" "humidity"]
;                     :config-map             {:type "analog"}
;                     }))
;
;(defn osc []
;      {:port (get* :osc/port)})
;
;(defn midi []
;      {:port (get* :midi/port)})
;
