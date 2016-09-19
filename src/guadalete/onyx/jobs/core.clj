(ns guadalete.onyx.jobs.core
    (:require
      [onyx.api]
      [guadalete.systems.rethinkdb.core :as db]
      [guadalete.onyx.jobs.scene :as scene-jobs]
      [guadalete.onyx.jobs.base :as base-jobs]
      [guadalete.onyx.jobs.development :as dev-jobs]
      [taoensso.timbre :as log]
      [guadalete.datastructures.flow :as flow]
      [guadalete.utils.util :refer [pretty validate!]]

      [ubergraph.core :as uber]
      [ubergraph.alg :as alg]

      [schema.core :as s]
      [guadalete.schema.core :as gs]))

(defn- make-graph [graph-description]
       (let [graph (->
                     (uber/digraph)
                     (uber/add-nodes-with-attrs* (->> (:nodes graph-description)
                                                      (map (fn [n] [(:id n) (:attrs n)]))))
                     (uber/add-edges* (->> (:edges graph-description)
                                           (map (fn [e] [(:from e) (:to e) (:attrs e)])))))]
            {:scene-id (keyword (:scene-id graph-description))
             :graph    graph}))

(defn make-jobs
      [{:keys [rethinkdb]}]
      (log/debug "**************** job-runner/make-jobs:")
      (with-open
        [db-conn (db/connect! rethinkdb)]
        (let [
              scenes (db/all-scenes db-conn)
              items (db/all-items db-conn)
              graph-jobs (->> (flow/assemble scenes items)
                              (map make-graph)
                              (scene-jobs/from-graphs))

              ;_ (log/debug "graph-jobs" (pretty graph-jobs))

              signal-config (base-jobs/signal-config-consumer)
              signal-value (base-jobs/signal-timeseries-consumer)
              light-config (base-jobs/light-config-consumer)
              all-jobs (conj graph-jobs signal-value signal-config)
              ;all-jobs (conj () signal-value signal-config light-config)
              ]
             all-jobs
             )))