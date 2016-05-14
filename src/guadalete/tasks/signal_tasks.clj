(ns guadalete.tasks.signal-tasks
    "Tasks specific to the signal-job"
    (:require [clojure
             [string :refer [capitalize trim]]
             [walk :refer [postwalk]]]
            [schema.core :as s]))

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
