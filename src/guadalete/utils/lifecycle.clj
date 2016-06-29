;;;; Lifecycles utils ;;;;
(ns guadalete.utils.lifecycle
    (:require
      [clojure.test :refer [is]]
      [clojure.set :refer [join]]
      [clojure.core.async :refer [chan sliding-buffer >!!]]
      [onyx.plugin.core-async :refer [take-segments!]]))

(def input-channel-capacity 10000)

(def output-channel-capacity (inc input-channel-capacity))

(defonce channels (atom {}))

(defn get-channel [id size]
      (or (get @channels id)
          (let [ch (chan size)]
               (swap! channels assoc id ch)
               ch)))

(defn get-input-channel [id]
      (get-channel id input-channel-capacity))

(defn get-output-channel [id]
      (get-channel id output-channel-capacity))

(defn channel-id-for [lifecycles task-name]
      (->> lifecycles
           (filter #(= task-name (:lifecycle/task %)))
           (map :core.async/id)
           (remove nil?)
           (first)))

(defn get-core-async-channels
      [{:keys [catalog lifecycles]}]
      (let [lifecycle-catalog-join (join catalog lifecycles {:onyx/name :lifecycle/task})]
           (reduce (fn [acc item]
                       (assoc acc
                              (:onyx/name item)
                              (get-channel (:core.async/id item)))) {} (filter :core.async/id lifecycle-catalog-join))))

(defn bind-inputs! [lifecycles mapping]
      (doseq [[task segments] mapping]
             (let [in-ch (get-input-channel (channel-id-for lifecycles task))]
                  (doseq [segment segments]
                         (>!! in-ch segment))
                  (>!! in-ch :done))))

(defn collect-outputs! [lifecycles output-tasks]
      (->> output-tasks
           (map #(get-output-channel (channel-id-for lifecycles %)))
           (map #(take-segments! %))))