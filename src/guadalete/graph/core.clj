(ns guadalete.graph.core
    (:require
      [ubergraph.core :as uber]
      [ubergraph.alg :as alg]
      [taoensso.timbre :as log]
      [guadalete.utils.util :refer [pretty in? validate! merge-keywords]]
      [schema.core :as s]
      [guadalete.schema.core :as gs]))

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

(defmulti replace
          (fn [node {:keys [ilk]} in-edges out-edges] ilk))

(defmethod replace :signal [node attributes _in-edges out-edges]
           (let [input-id (merge-keywords node :in)
                 input-attrs {:task-type :kafka/signals
                              :signal-id        (:id attributes)}

                 input-node [input-id input-attrs]
                 output-id (merge-keywords node :out)
                 output-attrs {:task-type :identity}
                 output-node [output-id output-attrs]
                 inside-edge {:src   input-id
                              :dest  output-id
                              :attrs {:gdlt/flow-filter :guadalete.onyx.filters/signal-id}}
                 out-edges* (map
                              (fn [edge]
                                  (cond-> {}
                                          (:src edge) (assoc :src output-id)
                                          (:dest edge) (assoc :dest (:dest edge))
                                          (:attrs edge) (assoc :attrs (:attrs edge))
                                          (nil? (:attrs edge)) (assoc :attrs {})))
                              out-edges)]
                {:nodes [input-node output-node]
                 :edges (conj out-edges* inside-edge)}))


(defmethod replace :color [node attributes in-edges out-edges]
           (let [attributes* (assoc attributes :task-type :color/node)]
                {:nodes [[node attributes*]]
                 :edges (into #{} (clojure.set/union in-edges out-edges))}))

(defmethod replace :light [node attributes in-edges out-edges]
           (let [attributes* (assoc attributes :task-type :light/node)]
                {:nodes [[node attributes*]]
                 :edges (into #{} (clojure.set/union in-edges out-edges))}))

(defmethod replace :mixer [node attributes in-edges out-edges]
           (let [attributes* (assoc attributes :task-type :mixer/node)]
                {:nodes [[node attributes*]]
                 :edges (into #{} (clojure.set/union in-edges out-edges))}))

(defn- replace-node [graph node]
       (let [attrs (uber/attrs graph node)
             in-edges (uber/in-edges graph node)
             out-edges (uber/out-edges graph node)
             replacement (replace node attrs in-edges out-edges)]

            (log/debug "-------------------")
            (log/debug "replace-node" node)
            ;(log/debug "\t attrs    " attrs)
            ;(log/debug "\t in-edges " (into [] in-edges))
            ;(log/debug "\t out-edges" (into [] out-edges))
            (log/debug "\t replacement" (pretty replacement))
            (log/debug "-------------------")

            (-> graph
                (uber/remove-nodes node)
                (uber/remove-edges* in-edges)
                (uber/remove-edges* out-edges)
                (uber/add-nodes-with-attrs* (:nodes replacement))
                (uber/add-edges*
                  (map
                    (fn [e] [(:src e) (:dest e) (or (:attrs e) {})])
                    (:edges replacement))))))

(s/defn expand-graph
        "Expands a graph by replacing the nodes (ie. signas, lights, colors…) with
        with subgraphs, each of which more closely represents an onyx task.
        Signal nodes for example, are breing substituted with two nodes and one edge between them:
        The first node represents reading the signal values from kafka and the second node filters by signal id."
        [graph]
        ;(log/debug "BEFORE:")
        ;(uber/pprint graph)
        ;(log/debug "----------------------------------------------------------------")
        (let [topological-ordering (alg/topsort graph)
              graph* (->> topological-ordering
                          (reduce replace-node graph))]
             ;(log/debug "----------------------------------------------------------------")
             ;(log/debug "AFTER:") (uber/pprint graph*)
             graph*))

(defn- make-scene-graph [[scene-id flows]]
       (let [
             graph (-> (uber/digraph)
                       ;(uber/add-nodes* (make-nodes flows))
                       (uber/add-nodes-with-attrs* (make-nodes flows))
                       (uber/add-edges* (make-edges flows))
                       (expand-graph))]
            ;(uber/pprint graph)
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

(defn leaf-nodes
      "Returns a collection of leaf nodes (ie. nodes with no outgoing edges) of a given graph."
      [graph]
      (nodes* graph uber/out-edges))
