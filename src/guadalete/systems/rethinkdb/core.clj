(ns guadalete.systems.rethinkdb.core
    (:require
      [com.stuartsierra.component :as component]
      [rethinkdb.query :as r]
      [taoensso.timbre :as log]
      [guadalete.utils.util :refer [pretty in? validate!]]
      [schema.core :as s]
      [guadalete.schema.core :as gs]))

;//   _            _      _
;//  | |__ ___ ___| |_ ___ |_ _ _ __ _ _ __
;//  | '_ \ _ \ _ \  _(_-<  _| '_/ _` | '_ \
;//  |_.__\___\___/\__/__/\__|_| \__,_| .__/
;//                                   |_|
(defn- bootstrap-db [conn db-name]
       (let [db-list (r/run (r/db-list) conn)
             db-exists? (in? db-list db-name)]
            (when-not db-exists?
                      (r/run (r/db-create db-name) conn))))

(defn- bootstrap-tables [conn db table-names]
       (let [existing-tables (r/run (r/table-list) conn)]
            (doseq [table-name table-names]
                   (log/debug "table exists?" table-name (in? existing-tables table-name))
                   (when-not (in? existing-tables table-name)
                             (r/run (r/table-create table-name) conn)))))




;//             _
;//   __ _ _ __(_)
;//  / _` | '_ \ |
;//  \__,_| .__/_|
;//       |_|
(defn connect! [{:keys [host port auth-key db]}]
      (log/debug "connnecting!" host port auth-key db)
      (r/connect :host host :port port :auth-key auth-key :db db))

(defn upsert!
      "upsert! takes a list of items [{:id \"item-1\" ...}, {:id \"item-2\" ...} ...]
       and insert them in the given table if the id is not present in the database, or does an update if the item already exists.\n\n"
      [conn table-name items]
      (let [table (r/table table-name)]
           (-> items
               (r/for-each
                 (r/fn [item]
                       ;; Find a document using the upsert'd item id.
                       (let [doc (r/get table (r/get-field item :id))]
                            (r/branch (r/eq nil doc)
                                      ;; Item is new, set its updated/created time and insert it.
                                      (r/insert table
                                                (r/merge {:updated (r/now) :created (r/now) :accepted? false} item)
                                                {:conflict "update"})
                                      ;; Item already exists, set the updated time and update the doc.
                                      ;; Take care of removing the id in the update-object to avoid upsetting RethinkDB.
                                      (r/update doc
                                                (r/merge {:updated (r/now)} (r/without item [:id])))))))
               (r/run conn))))



(defn all
      "Retrieves all things of the given type from all rooms"
      [conn type]
      (-> (r/table (name type))
          (r/run conn)))

(s/defn ^:always-validate all-scenes :- [gs/Scene]
        "Retrieves all scenes from all rooms"
        [conn]
        (let [scenes (-> (r/table "scene")
                         (r/run conn))]
             (log/debug "all scenes" (into [] scenes))
             (gs/coerce-scenes scenes)))

(defn all-lights
      "Retrieves all scenes from all rooms"
      [conn]
      (-> (r/table "light")
          (r/run conn)))




(defn- get-lights [light-ids conn]
       (->> (all-lights conn)
            (filter #(in? light-ids (:id %)))
            (into [])))

(defn- get-scenes [scene-ids conn]
       (->> (all-scenes conn)
            (filter #(in? scene-ids (:id %)))
            (into [])))


(defn- assemble-room [conn room]
       (let [lights (get-lights (:light room) conn)
             ;scenes (get-scenes (:scene room) conn)
             ]
            ;(assoc room :light lights :scene scenes)
            (assoc room :light lights)
            ))

(defn all-rooms
      "Retrieves all scenes from all rooms"
      [conn]
      (let [rooms (-> (r/table "room")
                      (r/run conn))]
           (->> rooms
                (map #(assemble-room conn %))
                (into []))))

(defn- purge
       "removes the :created and :updated fields from each entry in the given collection"
       [coll]
       (->> coll
            (map #(dissoc % :created :updated))))

(defn- coerce
       "removes the :created and :updated fields from each entry in the given collection"
       [type coll]
       (->> coll
            (map (fn [item]
                     (condp = type
                            :light (gs/coerce-light item)
                            :mixer (gs/coerce-mixer item)
                            :signal (gs/coerce-signal item)
                            :color (gs/coerce-color item)
                            :constant (gs/coerce-constant item))))))

(defn all-items
      "Retrieves all 'items' from the database.
      An item, in this context is anything that can be used in a PD graph, eg. lights, colors, signals, etc…"
      [conn]
      (let [items [:light :mixer :signal :color :constant]]
           (->> items
                (map (fn [i] [i (->> i
                                     (all conn)
                                     (purge)
                                     (coerce i)
                                     (into []))]))
                (into {}))))

;//    __ _
;//   / _| |_____ __ _____
;//  |  _| / _ \ V  V (_-<
;//  |_| |_\___/\_/\_//__/
;//

(defmulti assemble-item
          (fn [ilk flow-reference node item] ilk))

(defmethod assemble-item :signal [ilk flow-reference node item]
           ;(log/debug "assemble signal item")
           ;(log/debug "\t ilk" ilk)
           ;(log/debug "\t flow-reference" flow-reference)
           ;(log/debug "\t node" node)
           ;(log/debug "\t item" item)
           ;(log/debug "")
           {:id  (:id item)
            :ilk ilk})

(defmethod assemble-item :color [ilk flow-reference node item]
           ;(log/debug "assemble color item")
           ;(log/debug "\t ilk" ilk)
           ;(log/debug "\t flow-reference" flow-reference)
           ;(log/debug "\t node" node)
           ;(log/debug "\t item" item)
           ;(log/debug "")
           (let [id (str (:node-id flow-reference) "-" (:id flow-reference))]
                {:id id :ilk ilk}))

(defmethod assemble-item :mixer [ilk flow-reference node item]
           (log/debug "assemble mixer item" node item)
           item)

(defmethod assemble-item :light [ilk flow-reference node item]
           ;(log/debug "assemble light item")
           ;(log/debug "\t ilk" ilk)
           ;(log/debug "\t flow-reference" flow-reference)
           ;(log/debug "\t node" node)
           ;(log/debug "\t item" item)
           ;(log/debug "")
           (let [id (:id flow-reference)]
                {:id       id
                 :ilk      ilk
                 :channels (:channels item)}))


(defn- load-item [conn flow-reference]
       (let [
             nodes (->
                     (r/table "scene")
                     (r/get (:scene-id flow-reference))
                     (r/get-field :nodes)
                     (r/run conn))
             node (->> nodes
                       (vals)
                       (filter (fn [n] (= (:node-id flow-reference) (:id n))))
                       (first))
             ilk (keyword (:ilk node))
             item (-> (r/table (keyword (:ilk node)))
                      (r/get (str (:item-id node)))
                      (r/run conn))
             id (keyword (:id flow-reference))]
            (assemble-item ilk flow-reference node item)
            ))

(defn all-flows
      "Retrieves every flow from every scene"
      [conn]
      (-> (r/table "scene")
          (r/pluck [:id :flows])
          (r/map (r/fn [flow*] {:scene (r/get-field flow* :id) :flows (r/get-field flow* :flows)}))
          (r/run conn)))





;; r.table("posts")
;;   .filter(function (post) {
;;     return r.table("users")
;;       .filter(function (user) {
;;         return user("id").eq(post("authorId"))
;;       }).count().gt(0)
;;     })

;//                                           _
;//   __ ___ _ _  _ __  _ __ ___ _ _  ___ _ _| |_
;//  / _/ _ \ ' \| '  \| '_ \ _ \ ' \/ -_) ' \  _|
;//  \__\___/_||_|_|_|_| .__\___/_||_\___|_||_\__|
;//                    |_|
(defrecord RethinkDB [host port auth-key db tables]
           component/Lifecycle
           (start [component] component)
           (stop [component] component))

(defn new-rethinkdb [config]
      (map->RethinkDB config))