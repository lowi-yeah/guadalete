(ns guadalete.config.kafka
    (:require
      [guadalete.config.environment :as env])
    )

(def prefix "gdlt-")

(def topics
  {:signal-value  {:name               (str prefix "sgnl-v")
                   :partitions         1
                   :replication-factor 1
                   :config             {"cleanup.policy" "compact"}}
   :signal-config {:name               (str prefix "sgnl-c")
                   :partitions         1
                   :replication-factor 1
                   :config             {"cleanup.policy" "compact"}}
   :switch-value  {:name               (str prefix "swtch-v")
                   :partitions         1
                   :replication-factor 1
                   :config             {"cleanup.policy" "compact"}}
   :switch-config {:name               (str prefix "swtch-c")
                   :partitions         1
                   :replication-factor 1
                   :config             {"cleanup.policy" "compact"}}
   :artnet        {:name               (str prefix "artnet")
                   :partitions         1
                   :replication-factor 1
                   :config             {"cleanup.policy" "compact"}}

   :light-value   {:name               (str prefix "lght-v")
                   :partitions         1
                   :replication-factor 1
                   :config             {"cleanup.policy" "compact"}}
   :monitor       {:name               "kmf-topic"
                   :partitions         1
                   :replication-factor 1
                   :config             {"cleanup.policy" "compact"}}})



(defn kafka-topic [topic]
      (get-in topics [topic :name]))

(defn config* []
      {:zookeeper-address (env/get-value :zookeeper/address)
       :kafka-topics      (vals topics)})

(defn consumer []
      {"zookeeper.connect"           (env/get-value :zookeeper/address)
       "group.id"                    "signal-value.consumer"
       "auto.offset.reset"           "largest"
       "auto.commit.enable"          "true"
       "offsets.storage"             "kafka"
       "auto.commit.interval.ms"     "1000"
       ;"fetch.min.bytes"             "1"
       ;"socket.timeout.ms"           "1000"
       ;"socket.receive.buffer.bytes" "1024"
       ;"socket.receive.buffer.bytes" "128"
       })
