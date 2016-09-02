;//                                 _
;//   ___ _ _ _  ___ __  ____  _ ___ |_ ___ _ __
;//  / _ \ ' \ || \ \ / (_-< || (_-<  _/ -_) '  \
;//  \___/_||_\_, /_\_\ /__/\_, /__/\__\___|_|_|_|
;//           |__/          |__/

;// bootstrap onyx.

(ns guadalete.systems.onyx
    (:require
      [com.stuartsierra.component :as component]
      [onyx.plugin.core-async]
      [onyx.api]
      [taoensso.timbre :as log]
      [guadalete.utils.config :as config]
      [clojure.stacktrace :refer [print-stack-trace]]
      [guadalete.utils.util :refer [pretty]]))

(defrecord Onyx [use-env? n-peers peer-config env-config zookeeper bookkeeper]
           component/Lifecycle
           (start [component]
                  (log/info "\n\n**************** Starting Onyx ****************\n")
                  (log/info "\t zookeeper:" zookeeper)
                  (log/info "\t bookkeeper:" bookkeeper)
                  ;(log/info "java version: " (System/getProperty "java.runtime.version"))
                  ;(log/info "n-peers: " n-peers)
                  (try
                    (let [env (if use-env? (onyx.api/start-env env-config))
                          peer-group (onyx.api/start-peer-group peer-config)
                          peers (onyx.api/start-peers n-peers peer-group)]
                         (assoc component
                                :env env
                                :peer-group peer-group
                                :peers peers
                                :peer-config peer-config))
                    (catch Exception e
                      (log/error "ERROR in Onyx component" e)
                      (print-stack-trace e)
                      component)))

           (stop [component]
                 (log/info "Stopping Onyx")
                 (doseq [v-peer (:peers component)]
                        (onyx.api/shutdown-peer v-peer))
                 (when (:peer-group component)
                       (log/debug "shutdown peer group" (:peer-group component))
                       (onyx.api/shutdown-peer-group (:peer-group component)))
                 (when (:env component)
                       (log/debug "shutdown environment" (:env component))
                       (onyx.api/shutdown-env (:env component)))
                 (assoc component
                        :peer-group nil
                        :peer-config nil
                        :peers nil)))

(defn onyx [config]
      (map->Onyx config))