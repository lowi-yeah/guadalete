(ns guadalete.onyx.jobs.development
    (:require
      [schema.core :as s]
      [onyx.schema :as os]
      [taoensso.timbre :as log]
      [guadalete.onyx.jobs.util :refer [empty-job add-tasks]]
      [guadalete.onyx.tasks.kafka :as kafka-tasks]
      [guadalete.onyx.tasks.redis :as redis-tasks]
      [guadalete.onyx.tasks.rethink :as rethink-tasks]
      [guadalete.onyx.tasks.logging :as log-tasks]
      [guadalete.config.kafka :as kafka-config]
      [guadalete.config.task :as taks-config]
      [guadalete.config.onyx :refer [onyx-defaults]] [guadalete.onyx.tasks.async :as async-tasks]))

(defn dump-window! [event window-id lower-bound upper-bound state]
      (log/debug (format "Window extent %s, [%s - %s] contents: %s"
                       window-id lower-bound upper-bound state)))

(s/defn identity-task
        "Identitiy function task used to anchor lifecycle hooks"
        ([task-name :- s/Keyword]
          {:task   {:task-map (merge
                                (onyx-defaults)
                                {:onyx/name task-name
                                 :onyx/fn   :clojure.core/identity
                                 :onyx/type :function
                                 :onyx/doc  "The identity function"})}
           :schema {:task-map os/TaskMap}}))

(defn sum-init-fn [window]
      0)

(defn sum-aggregation-fn [window state segment]
      ; k is :age
      (let [k (second (:window/aggregation window))]
           [:set-value (+ state (get segment k))]))

(defn sum-application-fn [window state [changelog-type value]]
      (case changelog-type
            :set-value value))

;; sum aggregation referenced in window definition.
(def sum
  {:aggregation/init                sum-init-fn
   :aggregation/create-state-update sum-aggregation-fn
   :aggregation/apply-state-update  sum-application-fn})

(s/defn sum-signal-values
        ([task-name :- s/Keyword]
          {:task   {:task-map (merge
                                (onyx-defaults)
                                {:onyx/name           task-name
                                 :onyx/fn             :clojure.core/identity
                                 :onyx/type           :function
                                 :onyx/group-by-key   :id
                                 :onyx/flux-policy    :recover
                                 :onyx/uniqueness-key :at
                                 })

                    :windows  [{:window/id :collect-segments
                                :window/task :write-to-nowhere
                                :window/type :global
                                :window/aggregation :onyx.windowing.aggregation/conj
                                :window/window-key :at}]

                    :triggers [{:trigger/window-id :collect-segments
                                :trigger/refinement :onyx.refinements/accumulating
                                :trigger/on :onyx.triggers/segment
                                :trigger/threshold [5 :elements]
                                :trigger/sync ::dump-window!}]
                    }
           :schema {:task-map os/TaskMap
                    :windows  [os/Window]
                    :triggers  [os/Trigger]
                    }}))

;; Window definition



(defn window-test []
      (let [
            workflow [[:read-from-kafka :do-window-things]
                      [:do-window-things :write-to-nowhere]]
            tasks [
                   (kafka-tasks/signal-config-consumer :read-from-kafka)
                   (sum-signal-values :do-window-things)
                   (async-tasks/output
                     :write-to-nowhere {:task-opts
                                        (assoc (onyx-defaults) :onyx/uniqueness-key :at)
                                        :lifecycle-opts {:id :write-to-nowhere}})
                   ]
            job (-> empty-job
                    (add-tasks tasks)
                    (assoc :workflow workflow))
            ]
           {:name :dev/window-test
            :job  job}))

