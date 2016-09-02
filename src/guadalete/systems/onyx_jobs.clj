(ns guadalete.systems.onyx-jobs
    (:require
      [com.stuartsierra.component :as component]
      [onyx.api]
      [taoensso.timbre :as log]
      [guadalete.onyx.jobs.core :refer [make-jobs]]
      [guadalete.utils.config :as config]
      [clojure.stacktrace :refer [print-stack-trace]]))

(defn- start-job [peer-config {:keys [name job]}]
       (log/debug "ztartig job" name)
       (let [{:keys [job-id]} (onyx.api/submit-job peer-config job)]
            job-id))

(defn- stop-job [peer-config job-id]
       (log/debug "stopping job" job-id)
       (onyx.api/kill-job peer-config job-id))

(defn- start-jobs [peer-config jobs]
       (->> jobs
            (map (partial start-job peer-config))
            (into [])))

(defrecord JobRunner [onyx kafka mqtt rethinkdb]
           component/Lifecycle
           (start [component]
                  (log/info "starting component: JobRunner")
                  (try
                    (let [peer-config (:peer-config onyx)
                          jobs (make-jobs onyx kafka mqtt rethinkdb)
                          job-ids (start-jobs peer-config jobs)]
                         (assoc component :job-ids job-ids :peer-config peer-config))
                    (catch Exception e
                      (log/error "ERROR in JobRunner" e)
                      (print-stack-trace e)
                      component)))

           (stop [component]
                 (log/info "stopping component: JobRunner")
                 ;(doseq [job-id (:job-ids component)](stop-job (:peer-config component) job-id))
                 (dissoc component :job-ids :peer-config)))

(defn job-runner []
      (map->JobRunner {}))
