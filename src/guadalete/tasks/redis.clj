(ns guadalete.tasks.redis
    "A task for writing time series data into redis"
    (:require [clojure
               [string :refer [capitalize trim]]
               [walk :refer [postwalk]]]
      [taoensso.timbre :as log]
      [taoensso.carmine :as car :refer [wcar]]
      [cheshire.core :refer [generate-string]]
      [schema.core :as s]
      [onyx.plugin.redis]
      [onyx.schema :as os]
      [guadalete.schema.core :as gs]))

(defn insert!
      "inset!! takes a list of signal-events [{:id 'signal-id'…}, …]
       and writes them to redis as a time-series.
       @see: https://www.infoq.com/articles/redis-time-series"
      [conn prefix items]
      (doseq [item items]
             (let [id (car/wcar conn (car/incr (str prefix ":id")))
                   key (str prefix ":" id)
                   item* (assoc item :ev-id id)]
                  (car/wcar conn
                            (car/hmset* key item*)
                            (car/zadd (str "set:" prefix) (:at item*) (str id))))))

(defn write-event [event lifecycle]
      (let [connection (:redis/conn event)
            items (map #(:message %) (:onyx.core/batch event)) ; item: {:id "quicksine", :data 0, :at 1468916696730}
            ;_ (log/debug "write event!" (:onyx.core/batch event))
            prefix (:redis/prefix lifecycle)]
           (insert! connection prefix items)
           {}))

;;;;;;;;;;;;;;;;;;;;
;; Connection lifecycle code,
;; adapted from onyx.plugin.redis
(defn inject-conn-spec [{:keys [onyx.core/params onyx.core/task-map] :as event}
                        {:keys [redis/param? redis/uri redis/read-timeout-ms] :as lifecycle}]
      (when-not (or uri (:redis/uri task-map))
                (throw (ex-info ":redis/uri must be supplied to output task." lifecycle)))
      (let [conn {:spec {:uri             (or (:redis/uri task-map) uri)
                         :read-timeout-ms (or read-timeout-ms 4000)}}]
           {:onyx.core/params (if (or (:redis/param? task-map) param?)
                                (conj params conn)
                                params)
            :redis/conn       conn}))

(def rethink-lifecycle
  {:lifecycle/before-task-start inject-conn-spec
   :lifecycle/after-read-batch  write-event})

(s/defn output-task
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
                                       :lifecycle/calls ::rethink-lifecycle
                                       :onyx/param?     true
                                       :lifecycle/doc   "Initialises redis conn spec into event map"}
                                      lifecycle-opts)]}
         :schema {:lifecycles [os/Lifecycle]
                  ;:task-map   (merge os/TaskMap RedisTimeSeriesTask)
                  :task-map   os/TaskMap}})