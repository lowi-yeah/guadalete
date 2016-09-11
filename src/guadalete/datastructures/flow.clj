(ns guadalete.datastructures.flow
    (:require
      [ubergraph.core :as uber]
      [ubergraph.alg :as alg]
      [taoensso.timbre :as log]
      [guadalete.utils.util :refer [pretty validate!]]

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
;; 1. Take each FlowReference [from to id] and 'expand it' by attaching the referenced items to 'from' and 'to'



;//                        _    _
;//   __ _ _________ _ __ | |__| |___
;//  / _` (_-<_-< -_) '  \| '_ \ / -_)
;//  \__,_/__/__\___|_|_|_|_.__/_\___|
;//
(defn- get-node
       [nodes flow-segment]
       (get nodes (keyword (:node-id flow-segment))))

(defn- assemble-flow [{:keys [id from to] :as flow} nodes]
       (let [from-node (get-node nodes from)
             to-node (get-node nodes to)

             from-item-id (get-in from-node [:item :id])
             to-item-id (get-in to-node [:item :id])

             from-id (keyword from-item-id (:id from))
             to-id (keyword to-item-id (:id to))

             from* {:id   from-id
                    :ilk  (:ilk from-node)
                    :item (:item from-node)}
             to* {:id   to-id
                  :ilk  (:ilk to-node)
                  :item (:item to-node)}]

            {:id id :from from* :to to*}))

(s/defn assemble-flows
        [scene]
        (let [key (keyword (:id scene))
              flows (->> (:flows scene)
                         (map (fn [[id flow]] (assemble-flow flow (:nodes scene))))
                         (into []))]
             [key flows]))

(defn- attach-item
       [[node-id node] items]
       (let [ilk (:ilk node)
             item (->> (get-in items [ilk])
                       (filter #(= (:item-id node) (:id %)))
                       (first))
             links* (->> (:links node)
                         (map #(dissoc % :index :name :accepts :direction))
                         (into []))
             node* (-> node
                       (dissoc :item-id :position)
                       (assoc :item item)
                       (assoc :links links*))]
            [node-id node*]))

(defn- attach-items
       [scene items]
       (let [augmented-nodes (->> (:nodes scene)
                                  (map #(attach-item % items))
                                  (into {}))]
            (assoc scene :nodes augmented-nodes)))

(s/defn assemble
        "Assembles flows for further use from the raw data coming from rethinkDB"
        [scenes :- [gs/Scene]
         items]
        (try
          (->> scenes
               (filter #(not-empty (:flows %)))
               (map #(attach-items % items))
               (map #(assemble-flows %))
               (into {}))
          (catch Exception e (str "caught exception: " (.getMessage e)))))







;//   _                     __
;//  | |_ _ _ __ _ _ _  ___/ _|___ _ _ _ __
;//  |  _| '_/ _` | ' \(_-<  _/ _ \ '_| '  \
;//   \__|_| \__,_|_||_/__/_| \___/_| |_|_|_|
;//
(defn transform** [flow]
      (log/debug "transforming flow" (pretty flow))
      flow
      )

(defn transform* [[scene-id flows]]
      [scene-id (->> flows
                     (map transform**)
                     (into []))])

(defn transform [flow-map]
      (log/debug "transform" flow-map)
      (->> flow-map
           (map transform*)
           (into {}))
      flow-map)