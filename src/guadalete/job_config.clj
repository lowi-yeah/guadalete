(ns guadalete.job-config
    (:require
      [guadalete.jobs
       ;[switch-config]
       ;[signal-config]
       ;[signal-value]
       [artnet]]))

(defn make-jobs
      [_onyx _kafka _mqtt _rethinkdb artnet]
      [
       ;(guadalete.jobs.signal-value/build-job)
       ;(guadalete.jobs.switch-config/build-job)
       ;(guadalete.jobs.signal-config/build-job)
       (guadalete.jobs.artnet/build-job artnet)
       ])