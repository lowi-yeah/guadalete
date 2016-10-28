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
      [clojure.stacktrace :refer [print-stack-trace]]
      ))

(defn- bootstrap-topics
       "Check whether the given topics exists. If not, create them."
       [zookeeper topics]
       (with-open [zk (admin/zk-client zookeeper)]
                  (doseq [topic topics]
                         (if-not (admin/topic-exists? zk (:name topic))
                                 (do
                                   (admin/create-topic zk (:name topic)
                                                       {:partitions         (:partitions topic)
                                                        :replication-factor (:replication-factor topic)
                                                        :config             (:config-factor topic)}))))))

(defrecord Kafka [zookeeper topics]
           component/Lifecycle
           (start [component]
                  (log/info "**************** Starting Kafka component ***************")
                  (log/debug "**** zookeeper" zookeeper)
                  (log/debug "**** topics" topics)
                  (try
                    (bootstrap-topics zookeeper (vals topics))
                    (let [brokers (-> {"zookeeper.connect" zookeeper}
                                      (brokers)
                                      (broker-list))]
                         (log/debug "**** brokers" brokers)
                         (assoc component :brokers brokers))
                    (catch Exception e
                      (log/error "ERROR in Kafka component" e)
                      (print-stack-trace e)
                      component)))

           (stop [component]
                 (log/info "Stopping Kafka component")
                 (dissoc component :brokers)))

(defn kafka [config]
      (map->Kafka config))