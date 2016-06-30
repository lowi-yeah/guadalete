(ns guadalete.systems.rethinkdb.core
    (:require
      [com.stuartsierra.component :as component]
      [rethinkdb.query :as r]
      [taoensso.timbre :as log]
      [guadalete.utils.util :refer [pretty in?]]))

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

(defn all-scenes
      "Retrieves all scenes from all rooms"
      [conn]
      (let []
           (-> (r/table "scene")
               (r/run conn))))




(defn- scene-flows [scene*]
       (-> (r/get-field scene* :flows)
           ;(r/map (r/fn [flow*](r/get-field  flow* :id)))
           ))

(defn all-flows
      "Retrieves every flow from every scene"
      [conn]
      (try
        (let [flows* (->
                       (r/table "scene")
                       (r/pluck [:id :flows])
                       (r/map (r/fn [flow*] {:scene (r/get-field flow* :id)
                                             :flows (r/get-field flow* :flows)}))
                       (r/run conn))
              ()

              ]

             (log/debug "FLOWWS" (pretty flows*))

             flows*
             )
        (catch Exception e (str "caught exception: " (.getMessage e)))))

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