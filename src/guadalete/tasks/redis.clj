(ns guadalete.tasks.redis
    "A task for writing time series data into redis"
    (:require
      [clojure
       [string :refer [capitalize trim]]
       [walk :refer [postwalk]]]
      [taoensso.timbre :as log]
      [taoensso.carmine :as car :refer [wcar]]
      [cheshire.core :refer [generate-string]]
      [schema.core :as s]
      [onyx.tasks.redis :as onyx-redis]
      [onyx.schema :as os]
      [guadalete.schema.core :as gs]

      [guadalete.config.onyx :refer [onyx-defaults]]
      [guadalete.config.task :as taks-config]))


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



;//              _ _         _   _                          _
;//  __ __ ___ _(_) |_ ___  | |_(_)_ __  ___ ___ ______ _ _(_)___ ___
;//  \ V  V / '_| |  _/ -_) |  _| | '  \/ -_)___(_-< -_) '_| / -_)_-<
;//   \_/\_/|_| |_|\__\___|  \__|_|_|_|_\___|   /__\___|_| |_\___/__/
;//
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


(defn write-timeseries [event lifecycle]
      (let [connection (:redis/conn event)
            items (map #(:message %) (:onyx.core/batch event)) ; item: {:id "quicksine", :data 0, :at 1468916696730}
            ;_ (log/debug "write event!" (:onyx.core/batch event))
            prefix (:redis/prefix lifecycle)]
           (insert! connection prefix items)
           {}))

(def timeseries-lifecycle
  {:lifecycle/before-task-start inject-conn-spec
   :lifecycle/after-read-batch  write-timeseries})

(s/defn write-signals-timeseries
        [task-name :- s/Keyword]
        {:task   {:task-map   (merge
                                (onyx-defaults)
                                {:onyx/name   task-name
                                 :onyx/plugin :onyx.peer.function/function
                                 ;; We don't want to transform the data, just write it.
                                 ;; so we merely use the identity function.
                                 :onyx/fn     :clojure.core/identity
                                 :onyx/type   :output
                                 :onyx/medium :function
                                 :onyx/doc    "Does nothing (identity) but provide a place where lifecycle functions can hook into… "})

                  :lifecycles [(merge
                                 (taks-config/redis)
                                 {:lifecycle/task  task-name
                                  :lifecycle/calls ::timeseries-lifecycle
                                  :onyx/param?     true
                                  :lifecycle/doc   "Initialises redis conn spec into event map"})]}
         :schema {:lifecycles [os/Lifecycle]
                  ;:task-map   (merge os/TaskMap RedisTimeSeriesTask)
                  :task-map   os/TaskMap}})

(defn write!
      "write! takes a list of signal-events [{:id 'signal-id'…}, …]
       and writes them to redis as a plain old map entry."
      [conn prefix items]
      (doseq [item items]
             (let [key (str prefix ":" (:id item))
                   value (:data item)]
                  (car/wcar conn
                            (car/rpush key (dissoc item :id))))))

(defn write-value [event lifecycle]
      (let [connection (:redis/conn event)
            items (map #(:message %) (:onyx.core/batch event))
            prefix (:redis/prefix lifecycle)]
           (write! connection prefix items)
           {}))

(def value-lifecycle
  {:lifecycle/before-task-start inject-conn-spec
   :lifecycle/after-read-batch  write-value})

(s/defn write-signal-value
        [task-name :- s/Keyword]
        {:task   {:task-map   (merge
                                (onyx-defaults)
                                {:onyx/name   task-name
                                 :onyx/plugin :onyx.peer.function/function
                                 :onyx/fn     :clojure.core/identity
                                 :onyx/type   :output
                                 :onyx/medium :function
                                 :onyx/doc    "Does nothing (identity) but provide a place where lifecycle functions can hook into… "})

                  :lifecycles [(merge
                                 (taks-config/redis)
                                 {:lifecycle/task  task-name
                                  :lifecycle/calls ::value-lifecycle
                                  :onyx/param?     true
                                  :lifecycle/doc   "Initialises redis conn spec into event map"})]}
         :schema {:lifecycles [os/Lifecycle]
                  :task-map   os/TaskMap}})


;//                   _              _
;//   _ _ ___ __ _ __| |_____ ____ _| |_  _ ___
;//  | '_/ -_) _` / _` |___\ V / _` | | || / -_)
;//  |_| \___\__,_\__,_|    \_/\__,_|_|\_,_\___|
;//

(s/defn read-signal-values
        [task-name :- s/Keyword
         signal-id :- s/Keyword]
        (let [
              config* (merge
                        (onyx-defaults)
                        (taks-config/redis))

              key* (str (:redis/prefix config*) ":" (name signal-id))

              config** (merge
                         config*
                         {:redis/op  :lpop
                          :redis/key "sgnl:sine"})

              task* (onyx-redis/reader task-name config**)]

             (log/debug "onyx-task" task*)
             task*
             ))