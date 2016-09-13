(ns guadalete.onyx.tasks.identity
    (:require
      [schema.core :as s]
      [onyx.schema :as os]
      [taoensso.timbre :as log]
      [guadalete.onyx.tasks.util]

      [guadalete.config
       [onyx :refer [onyx-defaults]]
       [kafka :as kafka-config]
       [task :as task-config]
       ]
      ))

(defn log [called-by segment]
      (let [
            data (:data segment)
            δ (- (System/currentTimeMillis) (:at segment))]
           (log/debug (str called-by ": " segment " - " δ "ms"))
           segment))

(s/defn identity-task
        "Identitiy function task used to anchor lifecycle hooks"
        ([task-name :- s/Keyword]
          {:task   {:task-map (merge
                                (onyx-defaults)
                                {:onyx/name task-name
                                 :onyx/fn   :clojure.core/identity
                                 :onyx/type :function})}
           :schema {:task-map os/TaskMap}}))

(defn inject-called-by
      "Injects the mixin function (@see functions above)."
      [{:keys [onyx.core/task-map]} lifecycle]
      {:onyx.core/params [(:called-by task-map)]})

(def log-lifecycle-calls
  {:lifecycle/before-task-start inject-called-by})

(s/defn ^:always-validate log-task
        "Identitiy function task used to anchor lifecycle hooks"
        [task-name :- s/Keyword
         called-by :- s/Str]
        {:task   {:task-map   (merge
                                (onyx-defaults)
                                {:onyx/name task-name
                                 :onyx/fn   ::log
                                 :onyx/type :function
                                 :called-by called-by})
                  :lifecycles [{:lifecycle/task  task-name
                                :lifecycle/calls ::log-lifecycle-calls}]}
         :schema {:task-map   os/TaskMap
                  :lifecycles [os/Lifecycle]}})
