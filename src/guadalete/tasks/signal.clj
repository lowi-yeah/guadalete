(ns guadalete.tasks.signal
    "Signal specific tasks"
    (:require
      [clojure
       [string :refer [capitalize trim]]
       [walk :refer [postwalk]]]
      [guadalete.tasks.kafka :as kafka-task]
      [guadalete.config
       [task :as task-config]
       [onyx :refer [onyx-defaults]]]
      [schema.core :as s]
      [taoensso.timbre :as log]
      [guadalete.tasks.async :as async]))


(defn- make-kafka-topic [signal]
       (log/debug "making kafka topic for signal" signal)
       "gdlt-sgnl-v")

(defn read-value
      "Task which reads the values of a given signal from Kafka"
      [signal]
      (kafka-task/input-task
        :read-messages
        {:task-opts      (merge
                           (task-config/kafka-consumer)
                           {:kafka/topic        (make-kafka-topic signal)
                            :kafka/group-id     "signal-value-consumer"
                            :onyx/batch-size    1
                            :onyx/batch-timeout 1000})
         :lifecycle-opts {}}))

(defn async-subscribe [id]
      (async/subscribe-task
        (keyword id)
        {:task-opts      (onyx-defaults)
         :lifecycle-opts {:signal/id id}}))


;; I have no idea what that is:

(defn trim-in
      "Trims a string located at the keypath kp"
      [kp segment]
      (update-in segment kp trim))

(defn upper-case [{:keys [line] :as segment}]
      (if (seq line)
        (let [upper-cased (apply str (capitalize (first line)) (rest line))]
             (assoc-in segment [:line] upper-cased))
        segment))

(defn transform-segment-shape
      "Recursively restructures a segment {:new-key [paths...]}"
      [paths segment]
      (try (let [f (fn [[k v]]
                       (if (vector? v)
                         [k (get-in segment v)]
                         [k v]))]
                (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) paths))
           (catch Exception e
             segment)))

(defn get-in-segment [keypath segment]
      (get-in segment keypath))

(defn prepare-rows [segment]
      {:rows [segment]})
