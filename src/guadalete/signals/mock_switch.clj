(ns guadalete.signals.mock-switch
    (:require
      [com.stuartsierra.component :as component]
      [taoensso.timbre :as log]
      [clojure.core.async :refer [go go-loop <! >! >!! chan timeout alts!]]
      [clojurewerkz.machine-head.client :as mh]
      [thi.ng.math.core :as m]
      [cheshire.core :as json]
      [guadalete.signals.util :refer [swtch-value-topic swtch-config-topic run]]))

(def value-interval (* 1000 2))
(def config-interval (* 1000 10))
(def config-map {:num-channels 1 :type :capacitive})

(defn- send-value [conn id ]
       (try
         (mh/publish conn (swtch-value-topic id) "1")
         (Thread/sleep 200)
         (mh/publish conn (swtch-value-topic id) "0")
         (catch Exception e (str "caught exception: " (.getMessage e)))))

(defn- send-config [conn id]
       (mh/publish conn (swtch-config-topic id) (json/generate-string config-map)))


(defrecord MockSwitch [mqtt-broker mqtt-id]
           component/Lifecycle
           (start [component]
                  (log/debug "starting MockSwitch" mqtt-broker (swtch-value-topic mqtt-id))
                  (let [conn (mh/connect mqtt-broker mqtt-id)
                        value-stop-ch (run (partial send-value conn mqtt-id) value-interval)
                        config-stop-ch (run (partial send-config conn mqtt-id) config-interval)]
                       (log/info (str "MQTT (" mqtt-id "@" mqtt-broker ")"))
                       (log/debug "value-stop-ch" value-stop-ch)
                       (log/debug "config-stop-ch" config-stop-ch)
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

(defn new-mock-switch [config]
      (map->MockSwitch config))