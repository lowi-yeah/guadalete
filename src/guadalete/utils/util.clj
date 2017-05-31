(ns guadalete.utils.util
    (:require
      [clojure.edn :as edn]
      [environ.core :refer [env]]
      [cheshire.core :refer :all]
      [taoensso.timbre :as log]
      [schema.core :as s]
      [clj-time.core :as t]
      [clj-time.coerce :as tc]
      [schema.core :as s]
      [guadalete.config.zeroconf :as zeroconf]))

(defn deep-merge
      "Deep merge two maps"
      [& values]
      (if (every? map? values)
        (apply merge-with deep-merge values)
        (last values)))

(defn pretty
      "Returns a prettyprinted JSON representation of the argument"
      [argument]
      (generate-string argument {:pretty true}))

(defn in?
      "true if coll contains elm"
      [coll elm]
      (some #(= elm %) coll))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn kw* [something]
      (keyword something))

(defn mappify
      "Generic convenience function for converting a collection into a map."
      [map-key collection]
      (into {} (map (fn [x] {(name (get x map-key)) x}) collection)))

(defn now
      "milliseconds since Unix epoch"
      []
      (tc/to-long (t/now)))

(defn merge-keywords
      "merges the names of two keywords [:key-0 :key-1] into one namespaced keyword :key-0/key-1"
      [key-0 key-1]
      ;(keyword (str (name key-0) "-" (name key-1)))
      (keyword (name key-0) (name key-1))
      )

(defn validate!
      [schema data]
      (try
        (log/debug "validate" data)
        (log/debug "schema" schema)
        (s/validate schema data)
        (log/debug "**** VALID! ****")
        (catch Exception e
          (log/error "ERROR" e))))

(defn load-config []
      (reduce deep-merge (map (comp edn/read-string slurp)
                              [(:config-file env)])))

(defn configure-kafka [config zero-zookeeper]
      (try
        (let [zk-url (-> zero-zookeeper
                         (get :urls)
                         (first)
                         (clojure.string/replace #"http://" ""))]
             (-> config
                 (assoc-in [:kafka :zookeeper] zk-url)
                 (assoc-in [:kafka :kafka-consumer/config "zookeeper.connect"] zk-url)))
        (catch NullPointerException e
          config)))

(defn configure-mqtt [config zero-mqtt]
      (try
        (let [broker-url (-> zero-mqtt
                             (get :urls)
                             (first)
                             (clojure.string/replace #"http://" "tcp://"))]
             (assoc-in config [:mqtt :broker] broker-url))
        (catch NullPointerException e
          config)))

(defn configure-redis [config zero-redis]
      (try
        (let [uri (-> zero-redis
                      (get :urls)
                      (first)
                      (clojure.string/replace #"http://" "redis://"))]
             (assoc-in config [:redis :uri] uri))
        (catch NullPointerException e
          config)))

(defn configure-rethinkdb [config zero-rethink]
      (try
        (let [[host port] (-> zero-rethink
                              (get :urls)
                              (first)
                              (clojure.string/replace #"http://" "")
                              (clojure.string/split #":"))]
             (-> config
                 (assoc-in [:rethinkdb :host] host)
                 (assoc-in [:rethinkdb :port] port)))
        (catch NullPointerException e
          config)))

(defn configure-zookeeper [config zero-zookeeper]
      (try
        (let [zk-url (-> zero-zookeeper
                         (get :urls)
                         (first)
                         (clojure.string/replace #"http://" ""))]
             (-> config
                 (assoc-in [:zookeeper :zookeeper/address] zk-url)))
        (catch NullPointerException e
          config)))

(defn configure-onyx [config zero-zookeeper]
      (log/debug "zeroconfigure onyx")
      (try
        (let [zk-url (-> zero-zookeeper
                         (get :urls)
                         (first)
                         (clojure.string/replace #"http://" ""))
              tenancy-id (str (java.util.UUID/randomUUID))]
             (-> config
                 (assoc-in [:onyx :env-config :zookeeper/address] zk-url)
                 (assoc-in [:onyx :env-config :onyx/tenancy-id] tenancy-id)
                 (assoc-in [:onyx :peer-config :zookeeper/address] zk-url)
                 (assoc-in [:onyx :peer-config :onyx/tenancy-id] tenancy-id)))
        (catch Exception e
          (log/error "Error during Onyx zeroconfiguration" e)
          config)))

(defn configure [config servers]
      (log/debug "zeroconf-servers" (pretty servers))
      (-> config
          (configure-kafka (:zookeeper servers))
          (configure-mqtt (:mqtt servers))
          (configure-redis (:redis servers))
          (configure-rethinkdb (:rethinkdb servers))
          (configure-zookeeper (:zookeeper servers))
          (configure-onyx (:zookeeper servers))))

(defn load-zeroconfig []
      (let [config (load-config)
            zeroconf-servers (zeroconf/discover (:zeroconf config))]
           (configure config zeroconf-servers)))

(defn load-static-config []
      (let [config (load-config)]
           (configure config (:static-servers config))))

