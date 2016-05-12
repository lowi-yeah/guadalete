(ns guadalete.signals.weather
    (:require
      [com.stuartsierra.component :as component]
      [taoensso.timbre :as log]
      [clojure.core.async :refer [go go-loop <! >! >!! chan timeout alts!]]
      [clojurewerkz.machine-head.client :as mh]
      [thi.ng.math.core :as m]
      [cheshire.core :as json]
      [guadalete.signals.util :refer [value-topic config-topic run]]
      [schema.core :as s]
      [guadalete.schema.core :as gs]
      [forecast-clojure.core :refer [forecast]]
      [guadalete.utils.mock :as mock]
      ))


(s/defn send-value [conn id parameters]
        (let [
              ;weather (forecast "48.2228675" "16.3817916" :params {:units "si" :exclude "minutely,hourly,flags"})
              weather mock/weather]
             (try
               (doseq [p parameters]
                      (let [topic (value-topic (str id "-" p))
                            value (get-in weather [:currently (keyword p)])]
                           (mh/publish conn topic (str value))))
               (catch Exception e (str "caught exception: " (.getMessage e))))))


(defn- run-value [conn id parameters update-interval]
       (run (partial send-value conn id parameters) update-interval))

(defn- send-config [conn id parameters config-map]
       (doseq [p parameters]
              (let [topic (config-topic (str id "-" p))
                    config-map* (merge config-map {:name (str p) :description (str "The current " p " at la Donaira")})]
                   (mh/publish conn topic (json/generate-string config-map*)))))

(defn- run-config [conn id parameters config-map update-interval]
       (run (partial send-config conn id parameters config-map) update-interval))

(defrecord WeatherSignal [mqtt-broker mqtt-id parameters config-map value-update-interval config-update-interval]
           component/Lifecycle
           (start [component]
                  (log/debug "starting WeatherSignal" mqtt-broker mqtt-id parameters config-map value-update-interval config-update-interval)
                  (let [conn (mh/connect mqtt-broker mqtt-id)
                        value-stop-ch (run-value conn mqtt-id parameters value-update-interval)
                        config-stop-ch (run-config conn mqtt-id parameters config-map config-update-interval)]
                       (log/info (str "MQTT (" mqtt-id "@" mqtt-broker ")"))
                       (assoc component :conn conn :value value-stop-ch :configuration config-stop-ch))
                  )
           (stop [component]
                 (log/info "stopping WeatherSignal")
                 (let [conn (:conn component)
                       value-stop-ch (:value component)
                       config-stop-ch (:configuration component)]
                      (if (mh/connected? conn) (mh/disconnect conn))
                      (log/info (str "stopping component: WeatherSignal"))
                      (go
                        (>! value-stop-ch "halt!")
                        (>! config-stop-ch "halt!"))
                      (dissoc component :conn :value :configuration))))

(s/defn new-weather-signal [config :- gs/WeatherSignalConfig]
        (map->WeatherSignal config))


