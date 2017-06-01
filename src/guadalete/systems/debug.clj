(ns guadalete.systems.debug
    (:require
      [com.stuartsierra.component :as component]
      [guadalete.systems.mongodb :as db]
      [taoensso.timbre :as log]))

(defrecord Debug [mongodb]
           component/Lifecycle
           (start [component]
                  (log/info "starting component: Debug")
                  (log/info "mongodb: " mongodb)

                  (let [conn (db/connect! mongodb)
                        db   (db/get-db conn (:db mongodb))]
                       (log/debug "db" db)
                       (assoc component :conn conn)))

           (stop [component]
                 (log/debug "debug disconnecting" (:conn component))
                 (db/disconnect! (:conn component))
                 (dissoc component :conn)))

(defn debug [] (map->Debug {}))
