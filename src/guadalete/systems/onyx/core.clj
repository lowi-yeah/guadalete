;//                                 _
;//   ___ _ _ _  ___ __  ____  _ ___ |_ ___ _ __
;//  / _ \ ' \ || \ \ / (_-< || (_-<  _/ -_) '  \
;//  \___/_||_\_, /_\_\ /__/\_, /__/\__\___|_|_|_|
;//           |__/          |__/

;// bootstrap onyx.

(ns guadalete.systems.onyx.core
    (:require
      [com.stuartsierra.component :as component]
      [onyx.plugin.core-async]
      [onyx.api]
      [taoensso.timbre :as log]
      [guadalete.utils.config :as config]))

(defn- log-progress [msg data]
       (log/debug "**** **** ****" msg data))

(defrecord Onyx [n-peers peer-config]
           component/Lifecycle
           (start [component]
                  (log/info "\n\n**************** Starting Onyx ****************\n")
                  ;(log/info "java version: " (System/getProperty "java.runtime.version"))
                  ;(log/info "n-peers: " n-peers)
                  ;(log/info "peer-config: " peer-config)
                  (let [
                        onyx-id (java.util.UUID/randomUUID)
                        peer-config* (assoc peer-config :onyx/tenancy-id onyx-id)
                        peer-group (onyx.api/start-peer-group peer-config*)
                        peers (onyx.api/start-peers n-peers peer-group)
                        ]
                       (assoc component
                              :peer-group peer-group
                              :peers peers
                              :peer-config peer-config*
                              :onyx-id onyx-id))

                  )
           (stop [component]
                 (log/info "Stopping Onyx")
                 (doseq [v-peer (:peers component)]
                        (onyx.api/shutdown-peer v-peer))
                 (onyx.api/shutdown-peer-group (:peer-group component))
                 (assoc component
                        :peer-group nil
                        :peer-config nil
                        :peers nil)))

(defn new-onyx [config]
      (map->Onyx config))