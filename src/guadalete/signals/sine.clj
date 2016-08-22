(ns guadalete.signals.sine
    (:require
      [com.stuartsierra.component :as component]
      [taoensso.timbre :as log]
      [clojure.core.async :refer [go go-loop <! >! >!! chan timeout alts!]]
      [clojurewerkz.machine-head.client :as mh]
      [thi.ng.math.core :as m]
      [cheshire.core :refer [generate-string]]
      [guadalete.signals.util :refer [value-topic config-topic]]))


(def value-atom (atom 0))
(def config-interval 10000)

;(def config-map
;  {:desc "a 4.40Hz sine signal"
;   :type "analog"
;   :name "A-Major/centi"})

(defn- map* [x]
       (m/map-interval x -1.0 1.0 0.0 1.0))

(defn- send-value [conn id increment]
       (let [value-msg (str (map* (Math/sin @value-atom)))]
            (try
              (mh/publish conn (value-topic id) value-msg)
              (catch Exception e (str "caught exception: " (.getMessage e))))
            (swap! value-atom #(+ increment %))))

(defn- send-config [conn id]
       (mh/publish conn (config-topic id) (generate-string {:name id :type "analog"})))

(defn- run
       [f time-in-ms]
       (let [stop (chan)]
            (go-loop []
                     (let [timeout-ch (timeout time-in-ms)
                           [v ch] (alts! [timeout-ch stop])]
                          (when-not (= ch stop)
                                    (f)
                                    (recur))))
            stop))

(defn- run-value [conn id increment interval]
       (reset! value-atom (* -1 m/HALF_PI))
       (run (partial send-value conn id increment) interval))

(defn- run-config [conn id]
       (run (partial send-config conn id) config-interval))

(defrecord SineSignal [mqtt-broker mqtt-id increment interval]
           component/Lifecycle
           (start [component]
                  (log/debug "starting SineSignal" mqtt-id)
                  (let [conn (mh/connect mqtt-broker mqtt-id)
                        value-stop-ch (run-value conn mqtt-id increment interval)
                        config-stop-ch (run-config conn mqtt-id)]
                       (log/info (str "MQTT (" mqtt-id "@" mqtt-broker ")"))
                       (assoc component :conn conn :value value-stop-ch :configuration config-stop-ch)))
           (stop [component]
                 (let [conn (:conn component)
                       value-stop-ch (:value component)
                       config-stop-ch (:configuration component)]
                      (if (mh/connected? conn) (mh/disconnect conn))
                      (log/info (str "stopping component: SineSignal" mqtt-id))
                      (go
                        (>! value-stop-ch "halt!")
                        (>! config-stop-ch "halt!"))
                      (dissoc component :conn :value :configuration))))

(defn new-sine-signal [config]
      (map->SineSignal config))