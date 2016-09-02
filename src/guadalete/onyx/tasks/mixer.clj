(ns guadalete.onyx.tasks.mixer
    (:require
      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.schema.onyx :refer [KafkaInputTask]]
      [taoensso.timbre :as log]
      [guadalete.onyx.tasks.util]
      [guadalete.utils.util :refer [pretty validate!]]

      [guadalete.config
       [onyx :refer [onyx-defaults]]
       [kafka :as kafka-config]
       [task :as task-config]
       ]))

(defn dump-window! [event window-id lower-bound upper-bound state]
      (log/debug (format "Window extent %s, [%s - %s] contents: %s"
                         window-id lower-bound upper-bound state)))

(defn log-mixer [segment]
      (log/debug "mixxing" segment)
      segment)

(s/defn signal-mixer
        [task-name :- s/Keyword]

        (log/debug "signal-mixersignal-mixersignal-mixer" task-name)
        (let [
              task-map (merge
                         (onyx-defaults)
                         {:onyx/name           task-name
                          :onyx/fn             ::log-mixer
                          :onyx/type           :function
                          :onyx/uniqueness-key :at
                          :onyx/doc            "Logs the color to the console for debugging"})
              window {:window/id          :collect-segments
                      :window/task        task-name
                      :window/type        :global
                      :window/aggregation :onyx.windowing.aggregation/count
                      :window/window-key  :at}
              trigger {:trigger/window-id  :collect-segments
                       :trigger/refinement :onyx.refinements/accumulating
                       :trigger/on         :onyx.triggers/segment
                       :trigger/threshold  [5 :elements]
                       :trigger/sync       ::dump-window!}

              task {:task   {:task-map task-map
                             :windows  [window]
                             ;:triggers [trigger]
                             }
                    :schema {:task-map os/TaskMap
                             :windows  [os/Window]
                             ;:triggers [os/Trigger]
                             }}]
             task))
