(ns guadalete.onyx.jobs.core
    (:require
      [onyx.api]
      [guadalete.systems.rethinkdb.core :as db]
      [guadalete.onyx.jobs.scene :as scene-jobs]
      [guadalete.onyx.jobs.base :as base-jobs]
      [guadalete.onyx.jobs.development :as dev-jobs]
      [taoensso.timbre :as log]
      [guadalete.graph.core :as graph]
      [guadalete.utils.util :refer [pretty]]

      ))


(defn make-jobs
      [onyx kafka mqtt rethinkdb]
      (log/debug "**************** job-runner/make-jobs:")
      (with-open
        [db-conn (db/connect! rethinkdb)]
        (let [
              ;flows (db/all-flows db-conn)
              ;graph-map (graph/make-graphs flows)
              ;graph-jobs (scene-jobs/from-graphs graph-map)
              ;signal-config (base-jobs/signal-config-consumer)
              signal-value (base-jobs/signal-timeseries-consumer)
              window-test (dev-jobs/window-test)
              ;all-jobs (conj () signal-config )
              ;all-jobs (conj () window-test)
              ;all-jobs (conj () signal-value )
              all-jobs (conj () signal-value window-test)

              ]

             ;(log/debug "all-jobs" (pretty all-jobs))
             ;(log/debug "graph-jobs" (pretty graph-jobs))
             all-jobs
             ;graph-jobs
             ))
      )