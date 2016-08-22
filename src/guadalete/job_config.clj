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
       [signal-config]
       [signal-value]
       [signal-value-reader]
       [artnet]]))


(defn make-jobs
      ;[_onyx _kafka _mqtt rethinkdb artnet]
      [onyx kafka mqtt rethinkdb]

      (log/debug "\n****************\njob-runner/make-jobs:")
      (with-open
        [db-conn (db/connect! rethinkdb)]
        (let [
              ;flows (db/all-flows db-conn)
              ;flow-jobs (jobs/from-flows flows)

              debug-channel (chan)
              signal-value (guadalete.jobs.signal-value/build-job)
              signal-value-reader (guadalete.jobs.signal-value-reader/build-job)
              ]
             [
              signal-value
              signal-value-reader
              ;(guadalete.jobs.signal-config/build-job)
              ;(guadalete.jobs.switch-config/build-job)
              ;(guadalete.jobs.artnet/build-job artnet)
              ]
             )))