(ns guadalete.graph.node
    (:require
      [taoensso.timbre :as log]
      [schema.core :as s]
      [guadalete.schema.core :as gs]
      [guadalete.config.graph :refer [attributes-for-type]]
      [guadalete.utils.util :refer [pretty]]))

(s/defn ^:always-validate make-id :- s/Keyword
        [item-id :- s/Str
         link-id :- s/Str]
        (keyword (str item-id "-" link-id)))

(s/defn inlet? :- s/Bool
        [link :- gs/Link]
        (= :in (:direction link)))

(s/defn outlet? :- s/Bool
        [link :- gs/Link]
        (= :out (:direction link)))

(s/defn ^:always-validate node-description :- gs/NodeDescription
        "Creates a node description for creating an uber-node."
        [node :- gs/Node
         item
         link-id :- s/Str]
        (let [id (make-id (:id item) link-id)
              type (keyword (name (:ilk node)) (name link-id))
              attributes (attributes-for-type type node item)]
             {:id    id
              :attrs attributes}))

(s/defn ^:always-validate dissect-constant :- gs/NodeAndEdgeDescription
        "Dissect a Constant.
        Easy as… Becuse the constant but consists of one source-node emitting values"
        [node :- gs/Node
         item :- gs/Constant]
        (let [link (first (:links node))
              uber-node (node-description node item (:id link))]
             {:nodes [uber-node]
              :edges []}))

(s/defn ^:always-validate dissect-signal :- gs/NodeAndEdgeDescription
        "Dissect a Signal.
        Easy as… Becuse the signal but consists of one source-node emitting values"
        [node :- gs/Node
         item :- gs/Signal]
        (let [link (first (:links node))
              uber-node (node-description node item (:id link))]
             (log/debug "dissect-signal" uber-node)
             {:nodes [uber-node]
              :edges []}))

(s/defn ^:always-validate dissect-passthrough-node :- gs/NodeAndEdgeDescription
        "Passthrough nodes are nodes that have incoming as well as outging links
        — ie. they are neither sources nor sinks. All nodes of this kind
        (at least up to this point) share a common structure: When dissected,
        an additinal 'inner' node get created. In addition to the incoming & outgoing links, that is.
        All inlets are being connected to the inner node which in turn is connected to the out-node.
                  in ———
                        |
                  [..]————> inner ———> out
                        |
                  in ———  "
        [node :- gs/Node
         item]
        (let [in-nodes (->> (:links node)
                            (filter inlet?)
                            (map #(node-description node item (:id %))))
              inner-node (node-description node item "inner")
              out-nodes (->> (:links node)
                             (filter outlet?)
                             (map #(node-description node item (:id %))))

              in-edges (->> in-nodes
                            (map (fn [inlet]
                                     {:from (:id inlet)
                                      :to   (:id inner-node)})))

              out-edges (->> out-nodes
                             (map (fn [outlet]
                                      {:from (:id inner-node)
                                       :to   (:id outlet)})))

              uber-nodes (->>
                           (concat in-nodes [inner-node] out-nodes)
                           (into []))
              uber-edges (->>
                           (concat in-edges out-edges)
                           (into []))]
             {:nodes uber-nodes :edges uber-edges}))

(s/defn ^:always-validate dissect-mixer :- gs/NodeAndEdgeDescription
        [node :- gs/Node
         item :- gs/Mixer]
        (dissect-passthrough-node node item))

(s/defn ^:always-validate dissect-color :- gs/NodeAndEdgeDescription
        [node :- gs/Node
         item :- gs/Color]
        (dissect-passthrough-node node item))

(s/defn dissect-light :- [gs/NodeDescription]
        [node :- gs/Node
         item :- gs/Light]
        (let [link (first (:links node))
              link-node (node-description node item (:id link))
              sink-node (node-description node item "sink")
              uber-nodes [link-node sink-node]
              uber-edges [{:from (:id link-node)
                           :to   (:id sink-node)}]]
             {:nodes uber-nodes
              :edges uber-edges}))

(defmulti dissect
          "Given a node, return the concrete type to perform operations against."
          (fn [{:keys [node]}] (:ilk node)))

(defmethod dissect :constant
           [{:keys [node item]}]
           (dissect-constant node item))

(defmethod dissect :signal
           [{:keys [node item]}]
           (dissect-signal node item))

(defmethod dissect :mixer
           [{:keys [node item]}]
           (log/debug "dissect-mixer" node item)
           (dissect-mixer node item))

(defmethod dissect :color
           [{:keys [node item]}]
           (dissect-color node item))

(defmethod dissect :light
           [{:keys [node item]}]
           (dissect-light node item))
