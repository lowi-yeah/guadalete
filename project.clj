(defproject
  boot-project
  "0.0.0-SNAPSHOT"
  :dependencies
  [[aero "0.1.5" :exclusions [prismatic/schema]]
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
   [org.onyxplatform/onyx "0.9.4"]
   [org.onyxplatform/onyx-kafka "0.9.4.0"]
   [org.onyxplatform/onyx-redis "0.9.0.1"]
   [org.onyxplatform/lib-onyx "0.8.12.0-SNAPSHOT"]
   [com.taoensso/encore "2.52.1"]
   [com.taoensso/carmine "2.12.2"]
   [com.taoensso/timbre "4.3.1"]
   [thi.ng/math "0.2.1"]
   [forecast-clojure "1.0.3"]]
  :source-paths
  ["src" "lib/artnet4j-0001.jar"])