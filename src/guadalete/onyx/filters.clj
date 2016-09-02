(ns guadalete.onyx.filters
    (:require
      [schema.core :as s]
      [onyx.schema :as os]
      [taoensso.timbre :as log]

      [guadalete.config
       [task :as task-config]
       [onyx :refer [onyx-defaults]]
       [kafka :refer [kafka-topic]]]

      [guadalete.onyx.tasks
       [core :refer [ident]]
       [redis :as redis-tasks]
       [kafka :as kafka-tasks]
       [async :as async-tasks]
       ]

      [guadalete.utils.util :refer [pretty deep-merge]]
      ))

(defn signal-id [event old new all-new]
      (let [signal-id (get-in event [:onyx.core/task-map :filter/signal-id])]
           (= signal-id (:id new))))
