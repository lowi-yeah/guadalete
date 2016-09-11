(set-env!
  :source-paths #{"src"}
  :dependencies '[[lein-light-nrepl "0.3.3"]
                  [aero "0.1.5" :exclusions [prismatic/schema]]
                  [org.clojure/clojure "1.8.0"]
                  [org.clojure/core.async "0.2.374"]
                  [org.danielsz/system "0.2.0"]
                  [environ "1.0.2"]
                  [boot-environ "1.0.2"]
                  [cheshire "5.6.1"]
                  [clj-time "0.11.0"]
                  [clojurewerkz/machine_head "1.0.0-beta9"]
                  [com.apa512/rethinkdb "0.15.19"]
                  [org.clojure/tools.nrepl "0.2.12"]
                  [org.onyxplatform/onyx "0.9.9"]
                  [org.onyxplatform/onyx-kafka-0.8 "0.9.9.1-SNAPSHOT"]
                  ;[org.onyxplatform/onyx-kafka "0.9.4.0"]
                  [org.onyxplatform/onyx-redis "0.9.0.1"]
                  [org.onyxplatform/lib-onyx "0.9.7.1"]
                  [com.taoensso/encore "2.52.1"]
                  [com.taoensso/carmine "2.12.2"]
                  [com.taoensso/timbre "4.3.1"]
                  [thi.ng/math "0.2.1"]
                  [ubergraph "0.2.3"]
                  [forecast-clojure "1.0.3"]
                  [net.eliosoft/artnet4j "0001"]
                  [overtone/osc-clj "0.9.0"]
                  [overtone/midi-clj "0.5.0"]
                  [danlentz/clj-uuid "0.1.6"]
                  [javax.jmdns/jmdns "3.4.1"]
                  [commons-net "3.0.1"]
                  [overtone/at-at "1.1.1"]
                  ])
(require
  '[reloaded.repl :as repl :refer [start stop go reset]]
  '[guadalete.systems.core :refer [dev-system]]
  '[environ.boot :refer [environ]]
  '[system.boot :refer [system run]])

(def zk-dev-config
  {:zookeeper/address     "zookeeper1:2181"
   :zookeeper/server?     "false"
   :zookeeper.server/port 2181})

(def kafka-consumer-dev-config
  {:kafka-consumer/config
   {"zookeeper.connect" "zookeeper1:2181"
    "group.id"          "guadalete-ui.consumer"
    "auto.offset.reset" "smallest"
    ;"offsets.storage"             "kafka"
    ;"auto.commit.interval.ms"     "100"
    ;"auto.commit.enable"          "true"
    ;"fetch.min.bytes"             "1"
    ;"socket.timeout.ms"           "1000"
    ;"socket.receive.buffer.bytes" "1024"
    }})

(def onyx-dev-config
  {:onyx.peer/n-peers                     16
   ;:onyx.peer/job-scheduler               :onyx.job-scheduler/greedy
   ;:onyx.peer/job-scheduler               :onyx.job-scheduler/round-robin
   :onyx.peer/job-scheduler               :onyx.job-scheduler/balanced
   :onyx.messaging/impl                   :aeron
   :onyx.messaging/peer-port              40199
   :onyx.messaging/bind-addr              "localhost"
   :onyx.messaging/allow-short-circuit?   "false"
   :onyx.log/config                       {}
   :onyx.messaging.aeron/embedded-driver? "true"
   })

(def rethinkdb-dev-config
  {:rethinkdb/host     "127.0.0.1"
   :rethinkdb/port     28015
   :rethinkdb/auth-key ""
   :rethinkdb/db       "guadalete"
   :rethinkdb/tables   ["signal" "light" "room" "switch"]})

(def redis-dev-config
  {:redis/uri             "redis://redis1:6379"
   :redis/read-timeout-ms 8000})


(def mqtt-dev-config
  {:mqtt/broker "tcp://mosquitto1:1883"
   :mqtt/id     "guadalete-client"
   :mqtt/topics {"sgnl/#" 0 "swtch/#" 0}})

(def forecast-dev-config
  {:forecast-key "6c6ff80d697e050eff942334032eaa97"})

(def artnet-config
  {:artnet/address "10.17.0.201"})

(def osc-dev-config
  {:osc/port 12101})

(def midi-dev-config
  {:midi/port 6257})



(defn- dev-config
       "Merge the individual component configurations into one big map."
       []
       (merge zk-dev-config
              onyx-dev-config
              mqtt-dev-config
              rethinkdb-dev-config
              forecast-dev-config
              redis-dev-config
              osc-dev-config
              midi-dev-config
              kafka-consumer-dev-config))

(deftask dev
         "Run a restartable system in the r3pl."
         []
         (comp
           (environ :env (dev-config))
           (speak)
           (watch :verbose true)
           (system :sys #'dev-system :auto true :files ["onyx.clj"])
           (repl :server true)))

(deftask dev-run
         "Run a dev system from the command line"
         []
         (comp
           (environ :env {})
           (system :sys #'dev-system :auto true :files ["onyx.clj"])
           (run :main-namespace "guadalete.core" :arguments [#'dev-system])
           (wait)))

(deftask build
         "Builds an uberjar of this project that can be run with java -jar"
         []
         (comp
           (aot :namespace '#{guadalete.core})
           (pom :project 'myproject
                :version "1.0.0")
           (uber)
           (jar :main 'guadalete.core)))


;//   _     _           _            _
;//  | |___(_)_ _    ___ |_  __ _ __| |_____ __ __
;//  | / -_) | ' \  (_-< ' \/ _` / _` / _ \ V  V /
;//  |_\___|_|_||_| /__/_||_\__,_\__,_\___/\_/\_/
;//
;Cursive requires a project.clj file to infer some important information.
;The lein-generate task generates a project.clj file from this boot file so Cursive knows what's what.
(defn- generate-lein-project-file! [& {:keys [keep-project] :or {:keep-project true}}]
       (require 'clojure.java.io)
       (let [pfile ((resolve 'clojure.java.io/file) "project.clj")
             ; Only works when pom options are set using task-options!
             {:keys [project version]} (:task-options (meta #'boot.task.built-in/pom))
             prop #(when-let [x (get-env %2)] [%1 x])
             head (list* 'defproject (or project 'boot-project) (or version "0.0.0-SNAPSHOT")
                         (concat
                           (prop :url :url)
                           (prop :license :license)
                           (prop :description :description)
                           [:dependencies (get-env :dependencies)
                            :source-paths (vec (concat (get-env :source-paths)
                                                       (get-env :resource-paths)))]))
             proj (pp-str head)]
            (if-not keep-project (.deleteOnExit pfile))
            (spit pfile proj)))

(deftask make-lein
         "Generate a leiningen `project.clj` file.
          This task generates a leiningen `project.clj` file based on the boot
          environment configuration, including project name and version (generated
          if not present), dependencies, and source paths. Additional keys may be added
          to the generated `project.clj` file by specifying a `:lein` key in the boot
          environment whose value is a map of keys-value pairs to add to `project.clj`."
         []
         (generate-lein-project-file! :keep-project true))
