(ns guadalete.jobs.signal-configuration.component
    (:require
      [clojure.core.async :refer [>! >!! <! go-loop chan alts! close!]]
      [com.stuartsierra.component :as component]
      [onyx.api]
      [taoensso.timbre :as log]
      [onyx.plugin.core-async :refer [take-segments!]]
      [lib-onyx.log-subscriber :refer [start-log-subscriber stop-log-subscriber]]
      [guadalete.jobs.signal-configuration.job :as signal-configuration]
      [guadalete.tasks.core-async :refer [get-core-async-channels]]
      [guadalete.utils.lifecycle :refer [collect-outputs!]]))

(defn- run-signal-config! [rethink-config peer-config]
       (let [stop-channel (chan)
             job (signal-configuration/build-job rethink-config)
             {:keys [job-id]} (onyx.api/submit-job peer-config job)]
            job-id))

(defrecord SignalConfigurationJob [host port auth-key db table onyx]
           component/Lifecycle
           (start [component]
                  (log/info "starting component: SignalConfigurationJob")
                  (let [
                        peer-config (:peer-config onyx)
                        rethink-config {:rethink/host     host
                                        :rethink/port     port
                                        :rethink/auth-key auth-key
                                        :rethink/db       db
                                        :rethink/table    table}
                        job-id (run-signal-config! rethink-config peer-config)]
                       (assoc component :job-id job-id :peer-config peer-config)))
           (stop [component]
                 (log/info "stopping component: SignalConfigurationJob" (:job-id component))
                 (onyx.api/kill-job (:peer-config component) (:job-id component))
                 (dissoc component :job-id :peer-config)))

(defn signal-configuration-job [config]
      (map->SignalConfigurationJob config))