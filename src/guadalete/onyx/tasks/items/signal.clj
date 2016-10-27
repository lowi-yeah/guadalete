(ns guadalete.onyx.tasks.items.signal
    (:require
      [taoensso.timbre :as log]
      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.schema.core :as gs]
      [guadalete.utils.util :refer [pretty validate!]]
      [guadalete.config.onyx :refer [onyx-defaults]]
      [guadalete.onyx.tasks.kafka :as kafka-tasks]
      [guadalete.onyx.tasks.identity :refer [identity-task log-task dissoc-task dissoc-and-log-task]]))

(s/defn read
        [attributes]
        (let [task-name (:name attributes)
              group-id (:id attributes)
              signal-id (:signal-id attributes)]
             (kafka-tasks/signal-value-consumer task-name group-id signal-id)))

(defn coerce* [segment]
      (let [at* (read-string (:at segment))
            data* (read-string (:data segment))]
           (-> segment
               (assoc :at at*)
               (assoc :data data*))))


(s/defn coerce
        [attributes]
        (let [task-name (:name attributes)
              group-id (:id attributes)
              signal-id (:signal-id attributes)
              task-map (merge
                         (onyx-defaults)
                         {:onyx/name task-name
                          :onyx/fn   ::coerce*
                          :onyx/type :function
                          :onyx/doc  "Coerces a segment into a schema"})]
             {:task   {:task-map task-map}
              :schema {:task-map os/TaskMap}}))


