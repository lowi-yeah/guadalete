(ns guadalete.systems.zookeeper
    (:require [clojure.core.async :refer [chan >!! <!! close! thread alts!! offer!]]
      [com.stuartsierra.component :as component]
      [taoensso.timbre :refer [debug fatal warn info trace]]
      [onyx.log.curator :as zk]
      [onyx.extensions :as extensions]
      [onyx.static.default-vals :refer [defaults]]
      [onyx.compression.nippy :refer [zookeeper-compress zookeeper-decompress]]
      [onyx.log.replica :as replica]
      [onyx.monitoring.measurements :refer [measure-latency]]
      [onyx.log.entry :refer [create-log-entry]]
      [onyx.schema :as os]
      [schema.core :as s])
    (:import [org.apache.curator.test TestingServer]
      [org.apache.log4j BasicConfigurator]
      [org.apache.zookeeper KeeperException$NoNodeException KeeperException$NodeExistsException]))

(def root-path "/onyx")

(defn prefix-path [prefix]
      (assert prefix "Prefix must be supplied. Has :onyx/tenancy-id been supplied?")
      (str root-path "/" prefix))

(defn pulse-path [prefix]
      (str (prefix-path prefix) "/pulse"))

(defn log-path [prefix]
      (str (prefix-path prefix) "/log"))

(defn job-hash-path [prefix]
      (str (prefix-path prefix) "/job-hash"))

(defn catalog-path [prefix]
      (str (prefix-path prefix) "/catalog"))

(defn workflow-path [prefix]
      (str (prefix-path prefix) "/workflow"))

(defn flow-path [prefix]
      (str (prefix-path prefix) "/flow"))

(defn lifecycles-path [prefix]
      (str (prefix-path prefix) "/lifecycles"))

(defn windows-path [prefix]
      (str (prefix-path prefix) "/windows"))

(defn triggers-path [prefix]
      (str (prefix-path prefix) "/triggers"))

(defn job-metadata-path [prefix]
      (str (prefix-path prefix) "/job-metadata"))

(defn task-path [prefix]
      (str (prefix-path prefix) "/task"))

(defn sentinel-path [prefix]
      (str (prefix-path prefix) "/sentinel"))

(defn chunk-path [prefix]
      (str (prefix-path prefix) "/chunk"))

(defn origin-path [prefix]
      (str (prefix-path prefix) "/origin"))

(defn job-scheduler-path [prefix]
      (str (prefix-path prefix) "/job-scheduler"))

(defn messaging-path [prefix]
      (str (prefix-path prefix) "/messaging"))

(defn exception-path [prefix]
      (str (prefix-path prefix) "/exception"))

(defn throw-subscriber-closed []
      (throw (ex-info "Log subscriber closed due to disconnection from ZooKeeper" {})))

(defn clean-up-broken-connections [f]
      (try
        (f)
        (catch org.apache.zookeeper.KeeperException$ConnectionLossException e
          (trace e)
          (throw-subscriber-closed))
        (catch org.apache.zookeeper.KeeperException$SessionExpiredException e
          (trace e)
          (throw-subscriber-closed))))

(defn initialize-origin! [conn config prefix]
      (clean-up-broken-connections
        (fn []
            (let [node (str (origin-path prefix) "/origin")
                  bytes (zookeeper-compress {:message-id -1 :replica replica/base-replica})]
                 (zk/create conn node :data bytes :persistent? true)))))

(defrecord ZooKeeper [config]
           component/Lifecycle

           (start [component]
                  (s/validate os/PeerClientConfig config)
                  (info "Starting ZooKeeper" config)
                  (BasicConfigurator/configure)
                  (let [onyx-id (:onyx/tenancy-id config)
                        conn (zk/connect (:zookeeper/address config))
                        kill-ch (chan)]
                       (try
                         (zk/create conn root-path :persistent? true)
                         (zk/create conn (prefix-path onyx-id) :persistent? true)
                         (zk/create conn (pulse-path onyx-id) :persistent? true)
                         (zk/create conn (log-path onyx-id) :persistent? true)
                         (zk/create conn (job-hash-path onyx-id) :persistent? true)
                         (zk/create conn (catalog-path onyx-id) :persistent? true)
                         (zk/create conn (workflow-path onyx-id) :persistent? true)
                         (zk/create conn (flow-path onyx-id) :persistent? true)
                         (zk/create conn (lifecycles-path onyx-id) :persistent? true)
                         (zk/create conn (windows-path onyx-id) :persistent? true)
                         (zk/create conn (triggers-path onyx-id) :persistent? true)
                         (zk/create conn (job-metadata-path onyx-id) :persistent? true)
                         (zk/create conn (task-path onyx-id) :persistent? true)
                         (zk/create conn (sentinel-path onyx-id) :persistent? true)
                         (zk/create conn (chunk-path onyx-id) :persistent? true)
                         (zk/create conn (origin-path onyx-id) :persistent? true)
                         (zk/create conn (job-scheduler-path onyx-id) :persistent? true)
                         (zk/create conn (messaging-path onyx-id) :persistent? true)
                         (zk/create conn (exception-path onyx-id) :persistent? true)

                         (initialize-origin! conn config onyx-id)
                         (assoc component :conn conn :prefix onyx-id :kill-ch kill-ch)

                         (catch Exception e
                           (fatal "Error during zookeper initialization:" e)
                           component))))
           (stop [component]
                 (info "Stopping ZooKeeper")
                 (if (:conn component)
                   (zk/close (:conn component)))
                 (if (:kill-ch component)
                   (close! (:kill-ch component)))
                 component))

;(defmethod clojure.core/print-method ZooKeeper
;           [system ^java.io.Writer writer]
;           (.write writer "#<ZooKeeper Component>"))

(defn zookeeper [config]
      (map->ZooKeeper {:config config}))
