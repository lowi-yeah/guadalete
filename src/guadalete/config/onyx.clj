(ns guadalete.config.onyx
    (:require
      [guadalete.config.environment :as env]
      [taoensso.timbre :as log]))

(defn config* []
      (let [tenancy-id (str (str (java.util.UUID/randomUUID)))]
           (log/info "onyx tenancy-id:" tenancy-id)
           {:use-env?    false
            :n-peers     16
            :peer-config {:zookeeper/address                     (env/get-value :zookeeper/address)
                          :onyx.messaging/impl                   (env/get-value :onyx.messaging/impl)
                          :onyx.peer/job-scheduler               (env/get-value :onyx.peer/job-scheduler)
                          :onyx.messaging/peer-port              (env/get-value :onyx.messaging/peer-port)
                          :onyx.messaging/bind-addr              (env/get-value :onyx.messaging/bind-addr)
                          :onyx.log/config                       (env/get-value :onyx.log/config)
                          :onyx.messaging.aeron/embedded-driver? (env/get-bool :onyx.messaging.aeron/embedded-driver?)
                          :onyx/tenancy-id                       tenancy-id}
            :env-config  {:zookeeper/server?                  false
                          :zookeeper/address                  (env/get-value :zookeeper/address)
                          :onyx.bookkeeper/server?            true
                          :onyx.bookkeeper/delete-server-data?  true
                          :onyx.bookkeeper/local-quorum?      true
                          :onyx.bookkeeper/local-quorum-ports [48041 48042 48043]
                          :onyx.bookkeeper/base-journal-dir   "/Volumes/lowipro120/guadalete/bookkeeper/journal"
                          :onyx.bookkeeper/base-ledger-dir    "/Volumes/lowipro120/guadalete/bookkeeper/ledger"
                          ;:onyx.bookkeeper/zk-ledgers-root-path "/ledgers"
                          :onyx/tenancy-id                    tenancy-id}}))

(def onyx-batch* {:batch-size 10 :batch-timeout 1000})

(defn onyx-batch []
      {:onyx/batch-size    (:batch-size onyx-batch*)
       :onyx/batch-timeout (:batch-timeout onyx-batch*)})

(defn onyx-peer []
      {:onyx/min-peers 1
       :onyx/max-peers 1})

(defn onyx-defaults []
      (merge (onyx-peer) (onyx-batch)))
