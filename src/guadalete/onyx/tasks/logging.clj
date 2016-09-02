(ns guadalete.onyx.tasks.logging
    "Simple task for logging data to the console"
    (:require
      [schema.core :as s]
      [onyx.schema :as os]
      [taoensso.timbre :as log]
      [guadalete.config.onyx :refer [onyx-defaults]]))

(defn log! [segment]
      (log/debug "segment!" segment)
      segment
      )

(s/defn log
        [task-name :- s/Keyword]
        {:task   {:task-map   (merge {:onyx/name   task-name
                                      :onyx/plugin :onyx.peer.function/function
                                      :onyx/fn     ::log!
                                      :onyx/type   :function
                                      :onyx/doc    "Logs each segment to the console."}
                                     (onyx-defaults))
                  :lifecycles []}
         :schema {:task-map   os/TaskMap
                  :lifecycles [os/Lifecycle]}})