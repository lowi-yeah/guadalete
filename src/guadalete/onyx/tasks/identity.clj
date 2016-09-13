(ns guadalete.onyx.tasks.identity
    (:require
      [schema.core :as s]
      [onyx.schema :as os]
      [taoensso.timbre :as log]
      [guadalete.onyx.tasks.util]

      [guadalete.config
       [onyx :refer [onyx-defaults]]
       [kafka :as kafka-config]
       [task :as task-config]
       ]
      ))


(s/defn identity-task
        "Identitiy function task used to anchor lifecycle hooks"
        ([task-name :- s/Keyword]
          {:task   {:task-map (merge
                                (onyx-defaults)
                                {:onyx/name task-name
                                 :onyx/fn   :clojure.core/identity
                                 :onyx/type :function})}
           :schema {:task-map os/TaskMap}}))

