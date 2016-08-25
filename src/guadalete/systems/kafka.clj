;//   _         __ _                     _
;//  | |____ _ / _| |____ _   ____  _ ___ |_ ___ _ __
;//  | / / _` |  _| / / _` | (_-< || (_-<  _/ -_) '  \
;//  |_\_\__,_|_| |_\_\__,_| /__/\_, /__/\__\___|_|_|_|
;//                              |__/

;// bootstrap kafka.

(ns guadalete.systems.kafka
    (:require
      [com.stuartsierra.component :as component]
      [clj-kafka.admin :as admin]
      [clj-kafka.zk :refer [broker-list brokers topics]]
      [onyx.api]
      [taoensso.timbre :as log]
      ))

(defn- bootstrap-topics
       "Check whether the given topics exists. If not, create them."
       [zookeeper-address topics]
       (with-open [zk (admin/zk-client zookeeper-address)]
                  (doseq [topic topics]
                         (if-not (admin/topic-exists? zk (:name topic))
                                 (do
                                   (admin/create-topic zk (:name topic)
                                                       {:partitions         (:partitions topic)
                                                        :replication-factor (:replication-factor topic)
                                                        :config             (:config-factor topic)}))))))

(defrecord Kafka [zookeeper-address kafka-topics]
           component/Lifecycle
           (start [component]
                  (log/info "**************** Starting Kafka component ***************")
                  (log/debug "**** zookeeper-address" zookeeper-address)
                  (log/debug "**** kafka-topics" kafka-topics)
                  (bootstrap-topics zookeeper-address kafka-topics)
                  (let [brokers (-> {"zookeeper.connect" zookeeper-address}
                                     (brokers)
                                     (broker-list))]
                       (log/debug "**** brokers" (into [] brokers))
                       (assoc component :brokers brokers)))

           (stop [component]
                 (log/info "Stopping Kafka component")
                 (dissoc component :brokers)))

(defn new-kafka [config]
      (map->Kafka config))