(ns guadalete.job-config
    (:require
      [taoensso.timbre :as log]
      [rethinkdb.query :as r]
      [guadalete.systems.rethinkdb.core :as db]
      [guadalete.utils.util :refer [pretty]]
      [guadalete.jobs
       [switch-config]
       [signal-config]
       [signal-value]
       [artnet]]))

(defn make-jobs
      [_onyx _kafka _mqtt rethinkdb artnet]
      (log/debug "make-jobs" (pretty rethinkdb))
      (with-open
        [db-conn (db/connect! rethinkdb)]
        (let [scenes (db/all-scenes db-conn)
              flows (db/all-flows db-conn)]
             ;(log/debug "flows" (str flows))


             [
              ;(guadalete.jobs.signal-value/build-job)
              ;(guadalete.jobs.switch-config/build-job)
              ;(guadalete.jobs.signal-config/build-job)
              ;(guadalete.jobs.artnet/build-job artnet)
              ]
             )
        ))