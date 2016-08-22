(ns guadalete.config.rethinkdb
    (:require
      [guadalete.config.environment :as env]
      [taoensso.timbre :as log]))

(defn config* []
      {:host     (env/get-value :rethinkdb/host)
       :port     (env/get-value :rethinkdb/port)
       :auth-key (env/get-value :rethinkdb/auth-key)
       :db       (env/get-value :rethinkdb/db)
       :tables   (env/get-value :rethinkdb/tables)})
