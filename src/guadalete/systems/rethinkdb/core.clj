(ns guadalete.systems.rethinkdb.core
    (:require
      [com.stuartsierra.component :as component]
      [rethinkdb.query :as r]
      [taoensso.timbre :as log]
      [guadalete.utils.util :refer [in?]]))

(defn- bootstrap-db [conn db-name]
       (let [db-list (r/run (r/db-list) conn)
             db-exists? (in? db-list db-name)]
            (when-not db-exists?
                      (r/run (r/db-create db-name) conn))))

(defn- bootstrap-tables [conn db table-names]
       (let [existing-tables (r/run (r/table-list) conn)]
            (doseq [table-name table-names]
                   (log/debug "table exists?" table-name (in? existing-tables table-name))
                   (when-not (in? existing-tables table-name)
                             (r/run (r/table-create table-name) conn))
                   )))


(defrecord RethinkDB [host port auth-key db tables]
           component/Lifecycle
           (start [component]
                  (with-open [conn (r/connect :host host :port port :auth-key auth-key :db db)]
                             (log/info "Starting component: RethinkDB")
                             (bootstrap-db conn db)
                             (bootstrap-tables conn db tables))
                  component)

           (stop [component]
                 component))

(defn new-rethinkdb [config]
      (map->RethinkDB config))