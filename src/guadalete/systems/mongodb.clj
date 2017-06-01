(ns guadalete.systems.mongodb
    (:require
      [com.stuartsierra.component :as component]
      [monger.core :as mg]
      [monger.collection :as mc]
      [taoensso.timbre :as log]
      [guadalete.utils.util :refer [pretty in? validate!]]
      [schema.core :as s]
      [guadalete.schema.core :as gs])
    (:import [com.mongodb MongoOptions ServerAddress]))

;//   _            _      _
;//  | |__ ___ ___| |_ ___ |_ _ _ __ _ _ __
;//  | '_ \ _ \ _ \  _(_-<  _| '_/ _` | '_ \
;//  |_.__\___\___/\__/__/\__|_| \__,_| .__/
;//



;//             _
;//   __ _ _ __(_)
;//  / _` | '_ \ |
;//  \__,_| .__/_|
;//       |_|
(defn connect! [{:keys [host port auth-key]}]
      (log/debug "connnecting!" host port auth-key)
      ;; using MongoOptions allows fine-tuning connection parameters,
      ;; like automatic reconnection (highly recommended for production environment)
      (let [^MongoOptions opts (mg/mongo-options {})
            ;^MongoOptions opts (mg/mongo-options {:threads-allowed-to-block-for-connection-multiplier 300})
            ^ServerAddress sa (mg/server-address host port)]
           (mg/connect sa opts)))

(defn disconnect! [connection] (mg/disconnect connection))

(defn get-db [conn db]
      (log/debug "get-db!" conn db)
      (mg/get-db conn db))


;(defn upsert!
;      "upsert! takes a list of items [{:id \"item-1\" ...}, {:id \"item-2\" ...} ...]
;       and insert them in the given table if the id is not present in the database, or does an update if the item already exists.\n\n"
;      [conn table-name items]
;      (let [table (r/table table-name)]
;           (-> items
;               (r/for-each
;                 (r/fn [item]
;                       ;; Find a document using the upsert'd item id.
;                       (let [doc (r/get table (r/get-field item :id))]
;                            (r/branch (r/eq nil doc)
;                                      ;; Item is new, set its updated/created time and insert it.
;                                      (r/insert table
;                                                (r/merge {:updated (r/now) :created (r/now) :accepted? false} item)
;                                                {:conflict "update"})
;                                      ;; Item already exists, set the updated time and update the doc.
;                                      ;; Take care of removing the id in the update-object to avoid upsetting RethinkDB.
;                                      (r/update doc
;                                                (r/merge {:updated (r/now)} (r/without item [:id])))))))
;               (r/run conn))))




;//                                           _
;//   __ ___ _ _  _ __  _ __ ___ _ _  ___ _ _| |_
;//  / _/ _ \ ' \| '  \| '_ \ _ \ ' \/ -_) ' \  _|
;//  \__\___/_||_|_|_|_| .__\___/_||_\___|_||_\__|
;//                    |_|
(defrecord MongoDB [host port auth-key db tables]
           component/Lifecycle
           (start [component] component)
           (stop [component] component))

(defn mongodb [config]
      (log/debug "connecting to mongodb." config)
      (map->MongoDB config))