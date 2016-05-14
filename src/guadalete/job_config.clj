(ns guadalete.job-config
    (:require
      [guadalete.jobs
       [switch-config]
       [signal-config]
       [signal-value]]))

(defn make-jobs
      []
      [
       (guadalete.jobs.signal-value/build-job)
       (guadalete.jobs.switch-config/build-job)
       (guadalete.jobs.signal-config/build-job)
       ]
      )