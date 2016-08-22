(ns guadalete.systems.osc.core
    (:require
      [com.stuartsierra.component :as component]
      [taoensso.timbre :as log]
      [overtone.osc :refer :all]))

(defrecord OSC [port]
           component/Lifecycle
           (start [component]
                  (log/info "**************** Starting OSC server ***************")
                  (try
                    (let [server (osc-server port "foo")]
                         (osc-listen server (fn [msg] (log/debug "osc message: " msg)) :debug)
                         (assoc component :server server))
                    (catch Exception e (str "caught exception: " (.getMessage e)))))

           (stop [component]
                 (log/info "Stopping OSC component")
                 (try
                   (osc-rm-all-listeners (:server component))
                   (osc-close (:server component) 500)
                   (catch Exception e (str "caught exception: " (.getMessage e))))
                 (dissoc component :server)))

(defn new-osc [config] (map->OSC config))
