(ns guadalete.onyx.tasks.items.constant
    (:require
      [clojure.core.async :refer [chan >! >!! <!! close!]]
      [taoensso.timbre :as log]
      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.schema.core :as gs]
      [guadalete.utils.util :refer [pretty validate!]]
      [guadalete.onyx.tasks.kafka :as kafka-tasks]
      [guadalete.config
       [onyx :refer [onyx-defaults]]
       [task :as task-config]]

      [onyx.plugin.core-async :refer [take-segments!]]))

(s/defn in
        [attributes]
        (let [task-name (:name attributes)
              value (:value attributes)
              task-map (merge
                         (onyx-defaults)
                         (task-config/constant)
                         {:onyx/name      task-name
                          :onyx/type      :input
                          :onyx/plugin    :guadalete.onyx.plugin.constant/input
                          :onyx/medium    :constant
                          :constant/value value
                          :onyx/doc       "Emits a stream of but one value"})
              lifecycles [{:lifecycle/task  task-name
                           :lifecycle/calls :guadalete.onyx.plugin.constant/reader-calls}]]

             {:task   {:task-map   task-map
                       :lifecycles lifecycles}
              :schema {:task-map   os/TaskMap
                       :lifecycles [os/Lifecycle]}}))

