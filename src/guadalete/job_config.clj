(ns guadalete.job-config
    (:require
      [clojure.core.async :refer [chan >!! <!! close!]]
      [rethinkdb.query :as r]
      [ubergraph.core :as uber]
      [taoensso.timbre :as log]
      [guadalete.systems.rethinkdb.core :as db]
      [guadalete.jobs.core :as jobs]
      [guadalete.utils.util :refer [pretty]]
      [guadalete.jobs
       [switch-config]
       [signal-config :as signal-config]
       [signal-value :as signal-value]
       [artnet]]

      [guadalete.jobs.dev
       [signal-value-reader :as signal-value-reader]]
      ))


(defn make-jobs
      ;[_onyx _kafka _mqtt rethinkdb artnet]
      [onyx kafka mqtt rethinkdb]

      (log/debug "\n****************\njob-runner/make-jobs:")
      (with-open
        [db-conn (db/connect! rethinkdb)]
        (let [
              flows (db/all-flows db-conn)
              flow-jobs (jobs/from-flows flows)
              signal-value (signal-value/build-job)
              signal-config (signal-config/build-job)
              ;all-jobs (conj flow-jobs signal-value signal-config)]
              all-jobs (conj () signal-value signal-config)]
             (log/debug "all-jobs" all-jobs)
             all-jobs
             ;(log/debug "flow-jobs" flow-jobs)
             ;flow-jobs
             )))