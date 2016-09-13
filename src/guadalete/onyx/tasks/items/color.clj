(ns guadalete.onyx.tasks.items.color
    (:require
      [taoensso.timbre :as log]
      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.config.onyx :refer [onyx-defaults]]
      [guadalete.onyx.tasks.identity :refer [identity-task]]))

(defn log-color [color-segment]
      (log/debug "color-segment" color-segment)
      color-segment)

(s/defn in
        [{:keys [name channel]}]
        (let [task {:task   {:task-map (merge
                                         (onyx-defaults)
                                         {:onyx/name           name
                                          :onyx/fn             ::log-color
                                          :onyx/type           :function
                                          :onyx/uniqueness-key :at
                                          :onyx/doc            "Logs the color to the console for debugging"
                                          :color/channel       channel})}
                    :schema {:task-map os/TaskMap}}]
             task))

(s/defn inner
        [{:keys [name]}]
        (identity-task name))

(s/defn out
        [{:keys [name]}]
        (identity-task name))

