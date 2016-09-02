(ns guadalete.onyx.tasks.rethink
    "A task for writing data into rethinkDB"
    (:require [clojure
               [string :refer [capitalize trim]]
               [walk :refer [postwalk]]]
      [taoensso.timbre :as log]
      [cheshire.core :refer [generate-string]]
      [schema.core :as s]
      [onyx.schema :as os]
      [rethinkdb.query :as r]
      [guadalete.schema.core :as gs]))


(s/defschema RethinkSettings
             {(s/required-key :rethinkdb/host)     s/Str
              (s/required-key :rethinkdb/port)     s/Num
              (s/required-key :rethinkdb/auth-key) s/Str
              (s/required-key :rethinkdb/db)       s/Str
              (s/required-key :rethinkdb/table)    s/Str})

(s/defschema BatchSettings
             {(s/required-key :onyx/batch-size)    s/Num
              (s/required-key :onyx/batch-timeout) s/Num})

(s/defschema RethinkOutputTask {:rethinkdb/table s/Keyword
                                :rethinkdb/data  gs/Signal})


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

(defn write-to-database [event lifecycle]
      (let [db-connection (:db-connection event)
            table (:rethinkdb/table lifecycle)
            batch (:onyx.core/batch event)
            items
            (->> batch
                 (map #(assoc (get-in % [:message :data]) :id (get-in % [:message :id])))
                 (into []))]
           (when (not-empty items)
                 ;(log/debug "upsert!" items)
                 (upsert! db-connection table items))
           {}))

(defn connect [event lifecycle]
      (log/debug "connecting to RethinkDB")
      (let [host (:rethinkdb/host lifecycle)
            port (:rethinkdb/port lifecycle)
            auth-key (:rethinkdb/auth-key lifecycle)
            db (:rethinkdb/db lifecycle)
            db-connection (r/connect :host host :port port :auth-key auth-key :db db)]
           {:db-connection db-connection}))

(defn disconnect [event lifecycle]
      (log/debug "disconnecting from database")
      {})

(def rethink-lifecycle
  {:lifecycle/before-task-start connect
   :lifecycle/after-task-stop   disconnect
   :lifecycle/after-read-batch  write-to-database})

(s/defn output
        [task-name :- s/Keyword
         {:keys [task-opts lifecycle-opts] :as opts}]
        {:task   {:task-map   (merge {:onyx/name   task-name
                                      :onyx/plugin :onyx.peer.function/function
                                      ;; We don't want to transform the data, just write it.
                                      ;; so we merely use the identity function.
                                      :onyx/fn     :clojure.core/identity
                                      :onyx/type   :output
                                      :onyx/medium :function
                                      :onyx/doc    "Does nothing (identity) but provide a place where lifecycle functions can hook into… "}
                                     task-opts
                                     )
                  :lifecycles [(merge {:lifecycle/task  task-name
                                       :lifecycle/calls ::rethink-lifecycle}
                                      lifecycle-opts)
                               ]}
         :schema {:task-map   (merge os/TaskMap RethinkOutputTask)
                  :lifecycles [os/Lifecycle]}})

