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

(s/defn ^:always-validate dissect-signal :- gs/NodeAndEdgeDescription
        "Dissect a Signal.
        Easy as… Becuse the signal but consists of one source-node emitting signal values"
        [node :- gs/Node
         item :- gs/Signal]
        (let [link (first (:links node))
              uber-node (node-description node item (:id link))]
             (log/debug "dissect-signal" uber-node)
             {:nodes [uber-node]
              :edges []}))

(s/defn dissect-mixer :- gs/NodeAndEdgeDescription
        [node :- gs/Node
         item :- gs/Signal]
        (log/debug "dissect-mixer" node item)
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

              uber-nodes (concat in-nodes inner-node out-nodes)
              uber-edges (conj in-edges out-edges)]

             {:nodes uber-nodes :edges uber-edges}))

(s/defn dissect-color :- [gs/NodeDescription]
        [node :- gs/Node
         item :- gs/Color]
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

              uber-nodes (concat in-nodes [inner-node] out-nodes)
              uber-edges (concat in-edges out-edges)]

             (log/debug "dissect-color" (pretty uber-nodes))

             {:nodes uber-nodes :edges uber-edges}))

(s/defn dissect-light :- [gs/NodeDescription]
        [node :- gs/Node
         item :- gs/Light]
        (log/debug "dissect-light" node item)
        (let [link (first (:links node))
              link-node (node-description node item (:id link))
              sink-node (node-description node item "sink")

              _ (log/debug "\t link" link)
              _ (log/debug "\t link-node" link-node)
              _ (log/debug "\t sink-node" sink-node)

              uber-nodes [link-node sink-node]
              uber-edges [{:from (:id link-node)
                           :to   (:id sink-node)}]]
             {:nodes uber-nodes
              :edges uber-edges}))

(defmulti dissect
          "Given a node, return the concrete type to perform operations against."
          (fn [{:keys [node]}] (:ilk node)))

(defmethod dissect :signal
           [{:keys [node item]}]
           (dissect-signal node item))

(defmethod dissect :mixer
           [{:keys [node item]}]
           (dissect-mixer node item))

(defmethod dissect :color
           [{:keys [node item]}]
           (dissect-color node item))

(defmethod dissect :light
           [{:keys [node item]}]
           (dissect-light node item))
