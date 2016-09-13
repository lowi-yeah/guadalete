(ns guadalete.onyx.tasks.items.signal
    (:require
      [taoensso.timbre :as log]
      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.schema.core :as gs]
      [guadalete.utils.util :refer [pretty validate!]]
      [guadalete.onyx.tasks.kafka :as kafka-tasks]))

(s/defn in
        [attributes]
        (log/debug "TASK | signal/in" attributes)
        (let [task-name (:name attributes)
              group-id (:id attributes)
              signal-id (:signal-id attributes)]
             (kafka-tasks/signal-value-consumer task-name group-id signal-id)))

