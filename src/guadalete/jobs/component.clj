(ns guadalete.jobs.component
    (:require
      [com.stuartsierra.component :as component]
      [onyx.api]
      [taoensso.timbre :as log]
      [guadalete.job-config :refer [make-jobs]]
      [guadalete.utils.config :as config]))

(defn- start-job [peer-config {:keys [name job]}]
       (log/debug "ztartig job" name)
       (let [{:keys [job-id]} (onyx.api/submit-job peer-config job)]
            job-id)
       )

(defn- stop-job [peer-config job-id]
       (log/debug "stopping job" job-id)
       (onyx.api/kill-job peer-config job-id))

(defrecord JobRunner [onyx kafka mqtt rethinkdb]
           component/Lifecycle
           (start [component]
                  (log/info "starting component: JobRunner")
                  (let [peer-config (:peer-config onyx)
                        job-ids (into [] (map (partial start-job peer-config) (make-jobs onyx kafka mqtt rethinkdb)))]
                       (assoc component :job-ids job-ids :peer-config peer-config)))

           (stop [component]
                 (log/info "stopping component: JobRunner")
                 ;(doseq [job-id (:job-ids component)](stop-job (:peer-config component) job-id))
                 (dissoc component :job-ids :peer-config)))

(defn job-runner []
      (map->JobRunner {}))

