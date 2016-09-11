(ns guadalete.onyx.jobs.core
    (:require
      [onyx.api]
      [guadalete.systems.rethinkdb.core :as db]
      [guadalete.onyx.jobs.scene :as scene-jobs]
      [guadalete.onyx.jobs.base :as base-jobs]
      [guadalete.onyx.jobs.development :as dev-jobs]
      [taoensso.timbre :as log]
      [guadalete.datastructures.graph :as graph]
      [guadalete.datastructures.flow :as flow]
      [guadalete.utils.util :refer [pretty validate!]]
      [schema.core :as s]
      [guadalete.schema.core :as gs]))


(defn make-jobs
      [{:keys [rethinkdb]}]
      (log/debug "**************** job-runner/make-jobs:")
      (with-open
        [db-conn (db/connect! rethinkdb)]
        (let [
              scenes (db/all-scenes db-conn)
              items (db/all-items db-conn)

              mapp (-> scenes
                       (flow/assemble items)
                       (flow/transform)
                       )
              ;graph-map (graph/make-graphs flow-map)

              ;_ (log/debug "graph-map" graph-map)

              ;graph-jobs (scene-jobs/from-graphs graph-map)
              ;signal-config (base-jobs/signal-config-consumer)
              ;signal-value (base-jobs/signal-timeseries-consumer)
              ;all-jobs (conj () signal-value signal-config)
              all-jobs ()
              ]

             ;(log/debug "all-jobs" (pretty all-jobs))
             ;(log/debug "graph-jobs" (pretty graph-jobs))
             all-jobs
             ))
      )