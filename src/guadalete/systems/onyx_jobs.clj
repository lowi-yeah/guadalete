(ns guadalete.systems.onyx-jobs
    (:require
      [com.stuartsierra.component :as component]
      [onyx.api]
      [taoensso.timbre :as log]
      [guadalete.onyx.jobs.core :refer [make-jobs]]
      [guadalete.utils.config :as config]
      [guadalete.utils.util :refer [pretty]]
      [clojure.stacktrace :refer [print-stack-trace]]))

(defn- start-job [peer-config {:keys [name job]}]
       (let [{:keys [job-id]} (onyx.api/submit-job peer-config job)]
            job-id))

(defn- stop-job [peer-config job-id]
       (log/debug "stopping job" job-id)
       (onyx.api/kill-job peer-config job-id))

(defn- start-jobs [peer-config jobs]
       ;(log/debug "start-jobs" jobs)
       (->> jobs
            (map (partial start-job peer-config))
            (into [])))

(defrecord JobRunner [rethinkdb onyx]
           component/Lifecycle
           (start [component]
                  (log/info "starting component: JobRunner")
                  (try
                    (let [
                          peer-config (:peer-config onyx)
                          jobs (make-jobs {:rethinkdb rethinkdb})
                          job-ids (start-jobs peer-config jobs)]
                         (log/debug "\n****************************************************************\n\n ALL JOBS:\n" jobs "\n\n****************************************************************")
                         (assoc component :job-ids job-ids :peer-config peer-config))
                    (catch Exception ex
                      (log/error "ERROR in JobRunner" ex)
                      (print-stack-trace ex)
                      component)))

           (stop [component]
                 (log/info "stopping component: JobRunner")
                 (doseq [job-id (:job-ids component)]
                        (if (not (nil? job-id)) (stop-job (:peer-config component) job-id)))
                 (dissoc component :job-ids :peer-config)))

(defn job-runner []
      (map->JobRunner {}))
