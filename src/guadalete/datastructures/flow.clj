(ns guadalete.datastructures.flow
    (:require
      [ubergraph.core :as uber]
      [ubergraph.alg :as alg]
      [taoensso.timbre :as log]
      [guadalete.utils.util :refer [pretty validate!]]
      [guadalete.config.graph :refer [attributes-for-type]]
      [schema.core :as s]
      [guadalete.schema.core :as gs]))


;; # How to make the flow-graph
;; Upon startup, all scenes are beining loaded from the database and put into an ubergraph for further computations
;; To create the graph, the raw-data documents coming in from the db needs to be prepared in a way that the
;; ubergraph can be created easily.
;;
;; The incoming scene-data is a map of Scenes (@see guadalete.schema),
;; with each scene containing a number nodes (signals, functions,lights etc.)
;; and a number of FlowReferences (again, @see guadalete.schema…), that represent the edges of the graph.
;;
;; To create the graph, we need to transfrom the nodes into 'attribute nodes' [id attribute-map]
;; and the flow-references into 'edge descriptions' [src dest attribute-map]
;;
;; ## It goes like this:
;;
;; 1. Make the node descriptions [id attribute-map]


(defn- no-links?
       "Returns true if the node has no links in the given direction."
       [node direction]
       (->> (:links node)
            (filter #(= direction (:direction %)))
            (count)
            (= 0)))

(defn- source-node?
       "Checks whether or not the given node is a source node. Ie. whether or not the node has inlets."
       [node]
       (no-links? node :in))

(defn- sink-node?
       "Checks whether or not the given node is a sink. Ie. whether or not the node has outlets."
       [node]
       (no-links? node :out))

(defn- source-or-sink? [node]
       (or (source-node? node)
           (sink-node? node)))

(s/defn inlet?
        [link]
        (= :in (:direction link)))

(s/defn outlet?
        [link]
        (= :out (:direction link)))

(s/defn ^:always-validate outlets
        [node :- gs/Node
         item]
        (->> (:links node)
             (filter outlet?)
             (map (fn [link] {:id (keyword (:id item) (:id link))}))))

(s/defn ^:always-validate inlets
        [node :- gs/Node
         item]
        (->> (:links node)
             (filter inlet?)
             (map (fn [link] {:id (keyword (:id item) (:id link))}))))

(s/defn node-description
        "Creates node description for the 'inner' graph-node.
        Only applies for nodes that are neither source nor sink."
        [node :- gs/Node
         link-id :- s/Str
         item]
        (let [id (keyword (:id item) link-id)
              type (keyword (name (:ilk node)) (name link-id))
              attributes (attributes-for-type type node item)]
             {:id    id
              :attrs attributes}))

(s/defn inner-node :- gs/NodeDescription
        "Creates node description for the 'inner' graph-node.
        Only applies for nodes that are neither source nor sink."
        [node :- gs/Node
         item]
        (when (not (source-or-sink? node))
              (node-description node "inner" item)))

(s/defn border-nodes :- [gs/NodeDescription]
        "Creates node descriptions for the 'border' graph-nodes.
        The graph nodes that directly correspond to inlets/outlets.
        Sink and source nodes only have border nodes, inner nodes also have internal
        nodes (@see above)"
        [node :- gs/Node
         item]
        (->> (:links node)
             (map (fn [link] (node-description node (:id link) item)))
             (into [])))


(s/defn ^:always-validate dissect-node :- [gs/NodeDescription]
        "Takes a Node (ie NOT a ubergraph node) and dissects its inner workings.
        Returns a map of of ubergraph-node & edge descriptions.
        Depending on the type of node (Signal, Mixer, etc…) different approaches need to be taken.
        Source & Sink nodes (ie. nodes that have only incoming OR only outging links) are pretty easy to handle:
        Take the id of the node as ns and the id of the inlet/outlet as name,
        and return (keyword ns name) as the graph-node id"
        [{:keys [node item]}]
        (let [bn (border-nodes node item)
              in (inner-node node item)
              nodes (->>
                      (conj bn in)
                      (filter #(not (nil? %))))]
             ;(log/debug "dissect-node" node item)
             ;(log/debug "\t node" node)
             ;(log/debug "\t item" item)
             ;(log/debug "\n nodes " (pretty nodes))
             ;(log/debug "****")
             nodes))

(s/defn ^:always-validate load-item
        [node :- gs/Node
         items :- gs/Items]
        (let [item (->> (get-in items [(:ilk node)])
                        (filter #(= (:item-id node) (:id %)))
                        (first))]
             {:node node
              :item item}))

(s/defn ^:always-validate load-edge-items
        [{:keys [from to] :as flow-reference} :- gs/FlowReference
         nodes :- gs/Nodes
         items :- gs/Items]
        (let [from-node (get nodes (keyword (:node-id from)))
              from-item (->
                          (load-item from-node items)
                          (get-in [:item :id]))
              to-node (get nodes (keyword (:node-id to)))
              to-item (->
                        (load-item to-node items)
                        (get-in [:item :id]))]
             (-> flow-reference
                 (assoc-in [:from :item-id] from-item)
                 (assoc-in [:to :item-id] to-item))))

(s/defn ^:always-validate assemble-nodes
        [nodes :- [gs/Node]
         items :- gs/Items]
        (let [graph-nodes (->> nodes
                               (map #(load-item % items))
                               (map dissect-node)
                               (apply concat)
                               (into []))]
             ;(log/debug "graph-nodes" (pretty graph-nodes))
             graph-nodes))


(s/defn ^:always-validate inner-edges
        "Created the 'inner edges' for 'inner nodes' (ie. nodes that are neither springs nor sinks)."
        [{:keys [node item]}]
        (if (source-or-sink? node)
          []
          ;; else
          (let [inner-node* (inner-node node item)
                inlets* (inlets node item)
                outlets* (outlets node item)
                in-edges (->> inlets*
                              (map (fn [inlet]
                                       {:from (:id inlet)
                                        :to   (:id inner-node*)})))
                out-edges (->> outlets*
                               (map (fn [outlet]
                                        {:from (:id inner-node*)
                                         :to   (:id outlet)})))]
               (conj () in-edges out-edges))))

(s/defn ^:always-validate flow-edges
        [{:keys [from to]} :- gs/FlowReference]
        {:from (keyword (:item-id from) (:id from))
         :to   (keyword (:item-id to) (:id to))})

(s/defn ^:always-validate assemble-edges
        [flows :- [gs/FlowReference]
         nodes :- gs/Nodes
         items :- gs/Items]
        (let [inner-edges* (->> nodes
                                (vals)
                                (map #(load-item % items))
                                (map inner-edges))
              flow-edges* (->> flows
                               (map #(load-edge-items % nodes items))
                               (map flow-edges))
              edges* (-> ()
                         (conj inner-edges* flow-edges*)
                         (flatten))]
             edges*))

(s/defn ^:always-validate assemble-scene
        [scene :- gs/Scene
         items :- gs/Items]
        (let [nodes (assemble-nodes (vals (:nodes scene)) items)
              edges (assemble-edges (vals (:flows scene)) (:nodes scene) items)]
             {:scene-id (:id scene)
              :nodes    nodes
              :edges    (into [] edges)}))


(s/defn ^:always-validate assemble
        [scenes :- [gs/Scene]
         items :- gs/Items]
        (->> scenes
             (filter #(not-empty (:flows %)))
             (map #(assemble-scene % items))
             (flatten)
             (into []))
        ;(try
        ;  (catch Exception e (str "caught exception: " (.getMessage e))))
        )

