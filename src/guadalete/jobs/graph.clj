(ns guadalete.jobs.graph
    (:require
      [ubergraph.core :as uber]
      [ubergraph.alg :as alg]
      [taoensso.timbre :as log]
      [guadalete.utils.util :refer [pretty in?]]))

(defn- make-nodes
       "Internal helper for generating nodes from flow endpoints"
       [flows]
       (->> flows
            (map (fn [flow]
                     (let [from (:from flow)
                           to (:to flow)]
                          [[(-> from (get :id) (keyword)) from]
                           [(-> to (get :id) (keyword)) to]])))
            (apply concat)
            (into [])))

(defn- make-edges
       "Internal helper for generating edges from flows"
       [flows]
       (->> flows
            (map (fn [flow]
                     [(-> flow (get-in [:from :id]) (keyword))
                      (-> flow (get-in [:to :id]) (keyword))]))
            (into [])))

(defn- make-scene-graph [[scene-id flows]]
       (let [graph (-> (uber/digraph)
                       ;(uber/add-nodes* (make-nodes flows))
                       (uber/add-nodes-with-attrs* (make-nodes flows))
                       (uber/add-edges* (make-edges flows)))]
            [scene-id graph]))

(defn make-graphs
      "Constructs the scene-graphs from the given flows"
      [flow-map]
      (->> flow-map
           (filter (fn [[scene-id flows]] (not-empty flows)))
           (map make-scene-graph)
           (into {})))

(defn- nodes*
       "Generic helper function for finding either the source or leaf nodes of a graph"
       [graph map-fn]
       (->> graph
            (uber/nodes)
            (map (fn [node]
                     (let [num (->> node (map-fn graph) (count))]
                          [node num])))
            (filter (fn [[node num]]
                        (= 0 num)))
            (map (fn [[node num]] node))))

(defn source-nodes
      "Returns a collection of source nodes (ie. nodes with no incoming edges) of a given graph."
      [graph]
      (nodes* graph uber/in-edges))

(defn leaf-nodes [graph]
      (nodes* graph uber/out-edges))