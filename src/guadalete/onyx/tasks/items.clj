(ns guadalete.onyx.tasks.items
    "Namespace for tasks specific to guadalete items, ie. signals, colors & lights (for now…)"
    (:require
      [schema.core :as s]
      [onyx.schema :as os]
      [taoensso.timbre :as log]

      [guadalete.config
       [task :as task-config]
       [onyx :refer [onyx-defaults]]
       [kafka :refer [kafka-topic]]]

      [guadalete.onyx.tasks
       [redis :as redis-tasks]
       [kafka :as kafka-tasks]
       [async :as async-tasks]
       ]

      [guadalete.utils.util :refer [pretty deep-merge]]
      ))

(defn signal-in-question? [event old new all-new]
      (log/debug "filter signal" event old new all-new)
      true
      )


(defn- debug [key id task]
       (log/debug key id (-> task (get-in [:task :task-map]) (keys)))
       )

(s/defn ^:always-validate merge-lifecycles :- [os/Lifecycle]
        [tasks]
        (->> tasks
             (map #(get-in % [:task :lifecycles]))
             (apply concat)
             (into [])))

(s/defn signal
        "Generates the (sub-)job necessary for reading signal values from kafka.
        Works like this:
          -> subscribe to kafka-topic 'sgnl/v' on which all signal values are published
              λ in-task (kafka-tasks/signal-value-consumer)
          -> filter all incoming messages by :signal-id.
              λ out-task (ident)
              λ flow-condition [in-task -> out-task]
              λ workflow [in-task out-task]"
        [id :- s/Keyword]
        (let [in-id (keyword (name id) "in")
              out-id (keyword (name id) "out")
              kafka-consumer (kafka-tasks/signal-value-consumer in-id)
              identity* out-id
              lifecycles (merge-lifecycles [kafka-consumer identity*])
              flow-conditions [{:flow/from      in-id
                                :flow/to        out-id
                                :flow/predicate ::signal-in-question?}]]
             {:tasks           [kafka-consumer identity*]
              :workflows       [[in-id out-id]]
              :lifecycles      lifecycles
              :flow-conditions flow-conditions}))

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
      (let [task {:task   {:task-map (merge
                                       (onyx-defaults)
                                       {:onyx/name           task-id
                                        :onyx/fn             ::color-log
                                        :onyx/type           :function
                                        :onyx/uniqueness-key :at
                                        :onyx/doc            "Writes segments to a core.async channel"})}
                  :schema {:task-map os/TaskMap}}]
           {:tasks [task]}))

(defn light
      "Output task"
      [id]
      (let [task (async-tasks/output
                   id {:task-opts (onyx-defaults) :lifecycle-opts {:id id}})]
           {:tasks [task]}))
