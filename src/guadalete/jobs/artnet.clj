(ns guadalete.jobs.artnet
    (:require
      [onyx.api]
      [onyx.plugin.kafka]
      [taoensso.timbre :as log]
      [cheshire.core :as json]
      [onyx.schema :as os]
      [schema.core :as s]
      [guadalete.utils
       [job :refer [add-task add-tasks]]
       [config :as config :refer [kafka-topic]]
       [util :refer [now]]]
      [guadalete.tasks
       [artnet :as artnet-task]
       [kafka :as kafka-task]]))


(def base-job
  {:workflow       [[:read-messages :write-messages]]
   :lifecycles     []
   :catalog        []
   :task-scheduler :onyx.task-scheduler/balanced})

(defn configure-job
      [job artnet]
      (let [
            artnet-lifecycle-opts {:artnet/config-bytes      (:config-bytes artnet)
                                   :artnet/server-port       (:server-port artnet)
                                   :artnet/broadcast-address (:broadcast-address artnet)
                                   }
            job* (-> job
                     (add-task
                       (kafka-task/input-task
                         :read-messages
                         {:task-opts      (merge
                                            (config/kafka-task)
                                            {:kafka/topic (kafka-topic :artnet) :kafka/group-id "artnet-consumer"})
                          :lifecycle-opts {}}))

                     (add-task (artnet-task/output-task
                                 :write-messages
                                 {:task-opts      (merge (config/onyx-defaults) {:onyx/batch-timeout 200
                                                                                 :onyx/batch-size    1})
                                  :lifecycle-opts artnet-lifecycle-opts})))]


           job*))

(defn build-job [artnet]
      (log/debug "/n/n****/n building artnetn job" artnet)
      {:name :artnet
       :job  (configure-job base-job artnet)})

