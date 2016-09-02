(defproject
  boot-project
  "0.0.0-SNAPSHOT"
  :dependencies
  [[lein-light-nrepl "0.3.3"]
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
   [org.onyxplatform/onyx-kafka-0.8 "0.9.9.1-20160719.024045-1"]
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
   [overtone/at-at "1.1.1"]]

  :source-paths
  ["src"]

  :repl-options
  {:prompt (fn [ns] (str "\033[1;32m" ns "=>" "\033[0m "))}
  )