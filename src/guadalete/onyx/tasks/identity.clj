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

;//   _    _         _   _ _
;//  (_)__| |___ _ _| |_(_) |_ _  _
;//  | / _` / -_) ' \  _| |  _| || |
;//  |_\__,_\___|_||_\__|_|\__|\_, |
;//                            |__/
(s/defn identity-task
        "Identitiy function task used to anchor lifecycle hooks"
        ([task-name :- s/Keyword]
          {:task   {:task-map (merge
                                (onyx-defaults)
                                {:onyx/name task-name
                                 :onyx/fn   :clojure.core/identity
                                 :onyx/type :function})}
           :schema {:task-map os/TaskMap}}))

;//   _
;//  | |___ __ _
;//  | / _ \ _` |
;//  |_\___\__, |
;//        |___/
(defn log [called-by segment]
      (let [
            data (:data segment)
            δ (- (System/currentTimeMillis) (:at segment))]
           (log/debug (str called-by ": " segment " - " δ "ms"))
           segment))

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

;//      _ _
;//   __| (_)_________ __
;//  / _` | (_-<_-< _ \ _|
;//  \__,_|_/__/__\___\__|
;//
(defn do-dissoc
      "Dissasociates the given keys from the given segment."
      [keys segment]
      (apply dissoc segment keys))

(defn dissoc-and-log
      "Dissasociates the given keys from the given segment."
      [keys segment]
      (let [segment* (do-dissoc keys segment)]
           (log/debug segment*)
           segment*))

(defn inject-dissoc-keys
      "Injects the keys to be dissoced from the segemnt"
      [{:keys [onyx.core/task-map]} lifecycle]
      {:onyx.core/params [(:dissoc/keys task-map)]})

(def dissoc-lifecycle-calls
  {:lifecycle/before-task-start inject-dissoc-keys})

(s/defn ^:always-validate dissoc-task
        "Identitiy function task used to anchor lifecycle hooks"
        [task-name :- s/Keyword
         keys :- [s/Keyword]]
        {:task   {:task-map   (merge
                                (onyx-defaults)
                                {:onyx/name   task-name
                                 :onyx/fn     ::do-dissoc
                                 :onyx/type   :function
                                 :dissoc/keys keys})
                  :lifecycles [{:lifecycle/task  task-name
                                :lifecycle/calls ::dissoc-lifecycle-calls}]}
         :schema {:task-map   os/TaskMap
                  :lifecycles [os/Lifecycle]}})

(s/defn ^:always-validate dissoc-and-log-task
        [task-name :- s/Keyword
         keys :- [s/Keyword]]
        {:task   {:task-map   (merge
                                (onyx-defaults)
                                {:onyx/name   task-name
                                 :onyx/fn     ::dissoc-and-log
                                 :onyx/type   :function
                                 :dissoc/keys keys})
                  :lifecycles [{:lifecycle/task  task-name
                                :lifecycle/calls ::dissoc-lifecycle-calls}]}
         :schema {:task-map   os/TaskMap
                  :lifecycles [os/Lifecycle]}})

