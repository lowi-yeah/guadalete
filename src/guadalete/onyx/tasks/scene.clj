(ns guadalete.onyx.tasks.scene
    (:require
      [schema.core :as s]
      [onyx.schema :as os]
      [taoensso.timbre :as log]
      [guadalete.onyx.tasks.kafka :as kafka-tasks]
      [guadalete.onyx.tasks.mixer :as mixer-tasks]
      [guadalete.onyx.tasks.color :as color-tasks]
      [guadalete.config.onyx :refer [onyx-defaults]]
      [guadalete.onyx.tasks.async :as async-tasks]))


;//   _        _
;//  | |_  ___| |_ __ ___ _ _ ___
;//  | ' \/ -_) | '_ \ -_) '_(_-<
;//  |_||_\___|_| .__\___|_| /__/
;//             |_|

(defn log-segment [segment]
      ;(log/debug "segment" segment)
      segment)


(defn- color-task [task-name]
       (let [task {:task   {:task-map (merge
                                        (onyx-defaults)
                                        {:onyx/name           task-name
                                         :onyx/fn             ::log-color
                                         :onyx/type           :function
                                         :onyx/uniqueness-key :at
                                         :onyx/doc            "Logs the color to the console for debugging"})}
                   :schema {:task-map os/TaskMap}}]
            task))

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

(defn identity-log-task
      [task-name]
      {:task   {:task-map (merge
                            (onyx-defaults)
                            {:onyx/name task-name
                             :onyx/fn   ::log-segment
                             :onyx/type :function
                             :onyx/doc  "The identity function with a log"})}
       :schema {:task-map os/TaskMap}}
      )

;//               _         _           _
;//   _ _  ___ __| |___ ___| |_ __ _ ___ |__
;//  | ' \/ _ \ _` / -_)___|  _/ _` (_-< / /
;//  |_||_\___\__,_\___|    \__\__,_/__/_\_\
;//
(defmulti node-task
          (fn [{:keys [type]}] type))

(defmethod node-task :kafka/signals [_type id attrs]
           (kafka-tasks/signal-value-consumer
             id
             (str (namespace id) "-" (name id))
             (:signal-id attrs)))

;(defmethod node-task :identity [_type id attrs]
;           ;(identity-task id)
;           (identity-log-task id))
;
;(defmethod node-task :mixer/node [_type id {:keys [mix-fn] :as attrs}]
;           (mixer-tasks/signal-mixer id mix-fn))
;
;(defmethod node-task :color/node [_type id {:keys [type]}]
;           (color-tasks/color id (keyword type)))
;
;(defmethod node-task :light/node [_type id attrs]
;           (async-tasks/output id {:task-opts (onyx-defaults) :lifecycle-opts {:id id}}))
;
