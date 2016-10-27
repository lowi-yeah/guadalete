(ns guadalete.datastructures.flow
    (:require
      [clojure.set :as set]
      [ubergraph.core :as uber]
      [ubergraph.alg :as alg]
      [taoensso.timbre :as log]
      [guadalete.utils.util :refer [pretty validate!]]
      [guadalete.config.graph :refer [attributes-for-type]]
      [schema.core :as s]
      [guadalete.schema.core :as gs]
      [guadalete.graph.node :as node]))


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

(defn make-id
      [item-id link-id]
      (keyword (str item-id "-" link-id)))

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
             (map (fn [link] {:id (make-id (:id item) (:id link))}))))

(s/defn ^:always-validate inlets
        [node :- gs/Node
         item]
        (->> (:links node)
             (filter inlet?)
             (map (fn [link] {:id (make-id (:id item) (:id link))}))))

(s/defn node-description
        "Creates node description for the 'inner' graph-node.
        Only applies for nodes that are neither source nor sink."
        [node :- gs/Node
         link-id :- s/Str
         item]
        (let [id (make-id (:id item) link-id)
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

(s/defn ^:always-validate filter-linked-nodes
        "helper function for removing all nodes that are not linked correctly.
        Correct in this context means that ALL incoming & outgoing links are connected."
        [flows :- [gs/FlowReference]
         nodes :- [gs/Node]]
        (let [flow-links (->> flows
                              (map (fn [{:keys [from to] :as flow}]
                                       (let [in (keyword (:node-id from) (:id from))
                                             out (keyword (:node-id to) (:id to))]
                                            [in out])))
                              (apply concat)
                              (into #{}))
              filtered-nodes (->> nodes
                                  (filter (fn [{:keys [links] :as node}]
                                              (let [node-links (->> links
                                                                    (map (fn [link]
                                                                             (keyword (:id node) (:id link))))
                                                                    (into #{}))
                                                    difference (set/difference node-links flow-links)]
                                                   (empty? difference)))))]
             filtered-nodes))

(s/defn ^:always-validate assemble-nodes :- gs/NodeAndEdgeDescription
        [nodes :- [gs/Node]
         items :- gs/Items
         flows :- [gs/FlowReference]]
        (let [assembly {:nodes [] :edges []}
              graph-nodes (->> nodes
                               (filter-linked-nodes flows)
                               (map #(load-item % items))
                               (map #(node/dissect %))
                               (reduce (fn [result description]
                                           (-> result
                                               (assoc :nodes (concat (:nodes result) (:nodes description)))
                                               (assoc :edges (concat (:edges result) (:edges description)))))
                                       assembly))]
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
        {:from (make-id (:item-id from) (:id from))
         :to   (make-id (:item-id to) (:id to))})

(s/defn ^:always-validate assemble-edges
        [flows :- [gs/FlowReference]
         nodes :- gs/Nodes
         items :- gs/Items]
        (->> flows
             (map #(load-edge-items % nodes items))
             (map flow-edges)))

(s/defn ^:always-validate assemble-scene
        [scene :- gs/Scene
         items :- gs/Items]
        (let [{:keys [nodes edges]} (assemble-nodes (vals (:nodes scene)) items (vals (:flows scene)))
              outer-edges (assemble-edges (vals (:flows scene)) (:nodes scene) items)
              edges* (concat edges outer-edges)]
             {:scene-id (:id scene)
              :nodes    nodes
              :edges    edges*}))

(s/defn ^:always-validate assemble
        [scenes :- [gs/Scene]
         items :- gs/Items]
        (->> scenes
             (filter #(not-empty (:flows %)))
             (map #(assemble-scene % items))
             (flatten)
             (into [])))
