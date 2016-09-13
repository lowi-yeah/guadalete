(ns guadalete.onyx.tasks.items.mixer
    (:require
      [taoensso.timbre :as log]
      [schema.core :as s]
      [onyx.schema :as os]))

(s/defn in
        [attributes]
        (let [task-name (:name attributes)
              group-id (:id attributes)
              signal-id (:signal-id attributes)]
             (log/debug "mixer/in" task-name)
             ))


(s/defn mix!
        [attributes]
        {})

(s/defn out
        [attributes]
        (log/debug "mixer/out" attributes)
        {})
