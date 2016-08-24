(ns guadalete.tasks.items
    "Namespace for tasks specific to guadalete items, ie. signals, colors & lights (for now…)"
    (:require
      [schema.core :as s]
      [onyx.schema :as os]
      [taoensso.timbre :as log]

      [guadalete.config.task :as task-config]
      [guadalete.config.onyx :refer [onyx-defaults]]
      [guadalete.config.kafka :refer [kafka-topic]]

      [guadalete.tasks.async :as async]
      [guadalete.tasks.redis :as redis]
      [guadalete.tasks.kafka :as kafka]
      ))


(defn signal
      "Input task:
          -> get signal (via core.async/sub) [:id :value :at]
          -> filter by timestamp? (discard older than…)"
      ([id]
        (signal id id))
      ([task-id signal-id]
        (kafka/input-task
          :read-messages
          {:task-opts      (merge
                             (task-config/kafka)
                             {:kafka/topic        (kafka-topic :signal-value)
                              :kafka/group-id     (name task-id)
                              :onyx/batch-size    1
                              :onyx/batch-timeout 1000})
           :lifecycle-opts {}})

        ;(async/subscribe-task
        ;  task-id
        ;  {:task-opts      (onyx-defaults)
        ;   :lifecycle-opts {:signal/id (name signal-id)}})

        ))

(defn color-log [segment]
      (log/debug "colorrr" segment)
      segment)

(defn dump-window! [event window-id lower-bound upper-bound state]
      (log/debug (format "Window extent %s, [%s - %s] contents: %s"
                         window-id lower-bound upper-bound state)))

(defn color
      "Function task:
        -> receive signal segments
        -> a window aggregates the incoming signal values
      "
      [task-id]

      (let [window-id (keyword (str "w-" (name task-id)))]

           {:task   {:task-map   (merge
                                   {:onyx/name           task-id
                                    :onyx/fn             ::color-log
                                    :onyx/type           :function
                                    :onyx/uniqueness-key :at
                                    :onyx/doc            "Writes segments to a core.async channel"}
                                   (onyx-defaults))
                     :lifecycles []
                     ;:windows    [{:window/id          window-id
                     ;              :window/task        task-id
                     ;              :window/type        :global
                     ;              :window/aggregation :onyx.windowing.aggregation/conj
                     ;              :window/window-key  :event-time}]
                     ;:triggers   [{:trigger/window-id  window-id
                     ;              :trigger/refinement :onyx.refinements/accumulating
                     ;              :trigger/on         :onyx.triggers/segment
                     ;              :trigger/threshold  [5 :elements]
                     ;              :trigger/sync       ::dump-window!}]
                     }

            :schema {:task-map   os/TaskMap
                     :lifecycles [os/Lifecycle]
                     ;:windows    [os/Window]
                     ;:triggers   [os/Trigger]
                     }}
           ))


(defn light
      "Output task"
      [id]
      (async/output-task
        id
        {:task-opts      (onyx-defaults)
         :lifecycle-opts {:id id}}))
