(ns guadalete.onyx.tasks.items.color
    (:require
      [taoensso.timbre :as log]
      [schema.core :as s]
      [onyx.schema :as os]
      [clj-time.core :as t]
      [guadalete.config.onyx :refer [onyx-defaults]]
      [guadalete.onyx.tasks.identity :refer [identity-task log-task]]))

(defn log-color [color-segment]
      ;(log/debug "color-segment" color-segment)
      color-segment)


(s/defn in
        [{:keys [name channel] :as attributes}]
        ;(log-task name (str "color/" (clojure.core/name channel)))
        (identity-task name)
        )

(s/defn inner
        [{:keys [name] :as attributes}]
        (log/debug "TASK | color/inner" attributes)
        (let [task {:task   {:task-map (merge
                                         (onyx-defaults)
                                         {:onyx/name           name
                                          :onyx/fn             ::log-color
                                          :onyx/type           :function
                                          :onyx/uniqueness-key :at
                                          :onyx/doc            "Logs the color to the console for debugging"})}
                    :schema {:task-map os/TaskMap}}]
             task))

(s/defn out
        [{:keys [name]}]
        (identity-task name))

