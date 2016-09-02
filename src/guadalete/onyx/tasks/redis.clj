(ns guadalete.onyx.tasks.redis
    "Guadalete-specific tasks for writing data into redis."
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
      [conn namespace prefix items]
      (doseq [item items]
             (let [id (car/wcar conn (car/incr (str namespace ":" prefix ":id")))
                   key (str namespace ":" prefix ":" id)
                   item* (assoc item :ev-id id)]
                  (car/wcar conn
                            (car/hmset* key item*)
                            (car/zadd (str namespace ":" prefix) (:at item*) (str id))))))


(defn write-timeseries [event lifecycle]
      (let [connection (:redis/conn event)
            items (map #(:message %) (:onyx.core/batch event)) ; eg: {:id "sine", :data 0.64, :at 1468916696730}
            prefix (:redis/signal-prefix lifecycle)
            namespace (:redis/namespace lifecycle)]
           ;; sideffect
           (insert! connection namespace prefix items)
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
                                 ;; We don't want to transform the data, just write it. So we merely use the identity function to provide a hook.
                                 :onyx/fn     :clojure.core/identity
                                 :onyx/type   :output
                                 :onyx/medium :function
                                 :onyx/doc    "Provide an anchor where lifecycle functions can hook into… "})

                  :lifecycles [(merge
                                 (taks-config/redis)
                                 {:lifecycle/task  task-name
                                  :lifecycle/calls ::timeseries-lifecycle
                                  :onyx/param?     true
                                  :lifecycle/doc   "Initialises the redis-timeseries-lifecycle"})]}
         :schema {:lifecycles [os/Lifecycle]
                  :task-map   os/TaskMap}})
