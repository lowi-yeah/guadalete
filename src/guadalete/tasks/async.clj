(ns guadalete.tasks.async
    "A task for writing onyx data into async channels"
    (:require
      [clojure.core.async :refer [chan pub sub >!! <!! close!]]
      [taoensso.timbre :as log]
      [cheshire.core :refer [generate-string]]
      [schema.core :as s]
      [onyx.schema :as os]
      [onyx.api]
      [onyx.plugin.core-async]
      [guadalete.schema.core :as gs]
      [guadalete.jobs.state :as state]))


(defn inject-out-ch [event lifecycle]
      {:core.async/chan (state/out-channel (:id lifecycle))})
(def out-calls
  {:lifecycle/before-task-start inject-out-ch})

(s/defn output-task
        [task-name :- s/Keyword
         {:keys [task-opts lifecycle-opts] :as opts}]
        {:task   {:task-map   (merge
                                {:onyx/name   task-name
                                 :onyx/plugin :onyx.plugin.core-async/output
                                 :onyx/type   :output
                                 :onyx/medium :core.async
                                 :onyx/doc    "Writes segments to a core.async channel"}
                                task-opts)
                  :lifecycles [(merge
                                 {:lifecycle/task  task-name
                                  :lifecycle/calls ::out-calls}
                                 lifecycle-opts)
                               {:lifecycle/task  task-name
                                :lifecycle/calls :onyx.plugin.core-async/writer-calls}]}
         :schema {:task-map   os/TaskMap
                  :lifecycles [os/Lifecycle]}})

;//             _    _ _    _
;//   _ __ _  _| |__| (_)___ |_
;//  | '_ \ || | '_ \ | (_-< ' \
;//  | .__/\_,_|_.__/_|_/__/_||_|
;//  |_|
(defn inject-pub-ch [event lifecycle]
      (let [channel (state/publication-channel :signal/value)]
           {:core.async/chan channel}))

(def publish-calls
  {:lifecycle/before-task-start inject-pub-ch})

(s/defn publish-task
        [task-name :- s/Keyword
         {:keys [task-opts] :as opts}]
        {:task   {:task-map   (merge {:onyx/name   task-name
                                      :onyx/plugin :onyx.plugin.core-async/output
                                      :onyx/type   :output
                                      :onyx/medium :core.async
                                      :onyx/doc    "Publishes segments to core.async"}
                                     task-opts
                                     )

                  :lifecycles [{:lifecycle/task  task-name
                                :lifecycle/calls ::publish-calls}
                               {:lifecycle/task  task-name
                                :lifecycle/calls :onyx.plugin.core-async/writer-calls}]}
         :schema {:lifecycles [os/Lifecycle]
                  :task-map   os/TaskMap}})

;//           _              _ _
;//   ____  _| |__ _____ _ _(_) |__ ___
;//  (_-< || | '_ (_-< _| '_| | '_ \ -_)
;//  /__/\_,_|_.__/__\__|_| |_|_.__\___|
;//


(defn inject-sub-ch [event lifecycle]
      (let [channel (state/subscribe :signal/value (:signal/id lifecycle))]
           {:core.async/chan channel}))

(def subscribe-calls
  {:lifecycle/before-task-start inject-sub-ch})


(s/defn subscribe-task
        [task-name :- s/Keyword
         {:keys [task-opts lifecycle-opts] :as opts}]

        {:task   {:task-map   (merge {:onyx/name   task-name
                                      :onyx/plugin :onyx.plugin.core-async/input
                                      :onyx/type   :input
                                      :onyx/medium :core.async
                                      :onyx/doc    "Reads segments from a core.async publication"}
                                     task-opts
                                     )

                  :lifecycles [(merge
                                 lifecycle-opts
                                 {:lifecycle/task  task-name
                                  :lifecycle/calls ::subscribe-calls})
                               {:lifecycle/task  task-name
                                :lifecycle/calls :onyx.plugin.core-async/reader-calls}]}
         :schema {:lifecycles [os/Lifecycle]
                  :task-map   os/TaskMap}})
