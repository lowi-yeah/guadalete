(ns guadalete.job-config
    (:require
      [clojure.core.async :refer [chan >!! <!! close!]]
      [rethinkdb.query :as r]
      [ubergraph.core :as uber]
      [taoensso.timbre :as log]
      [guadalete.systems.rethinkdb.core :as db]

      [guadalete.utils.util :refer [pretty]]

      [guadalete.onyx.jobs.base :as base-jobs]
      ;[guadalete.jobs
      ; [scenes :as scene-jobs]
      ; [switch-config]
      ; [signal-config :as signal-config]
      ; [signal-value :as signal-value]
      ; [artnet]]
      ;
      ;[guadalete.jobs.dev
      ; [signal-value-reader :as signal-value-reader]]
      ))


(defn make-jobs
      [onyx kafka mqtt rethinkdb]
      (log/debug "**************** job-runner/make-jobs:")
      (with-open
        [db-conn (db/connect! rethinkdb)]
        (let [
              ;flows (db/all-flows db-conn)
              ;flow-jobs (scene-jobs/from-flows flows)
              signal-value (base-jobs/signal-timeseries-consumer)
              ;signal-config (signal-config/build-job)
              ;all-jobs (conj flow-jobs signal-value signal-config)]
              all-jobs (conj () signal-value)]
             (log/debug "all-jobs" all-jobs)
             ;all-jobs

             []
             )))