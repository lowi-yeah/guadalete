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
                  [thi.ng/color "1.2.0"]
                  [clojurewerkz/statistiker "0.1.0-SNAPSHOT"]
                  [ubergraph "0.2.3"]
                  [thi.ng/tweeny "0.1.0-SNAPSHOT"]
                  [forecast-clojure "1.0.3"]
                  [net.eliosoft/artnet4j "0001"]
                  [overtone/osc-clj "0.9.0"]
                  [overtone/midi-clj "0.5.0"]
                  [danlentz/clj-uuid "0.1.6"]
                  [javax.jmdns/jmdns "3.4.1"]
                  [commons-net "3.0.1"]
                  [eu.cassiel/clojure-zeroconf "1.2.0"]
                  [overtone/at-at "1.1.1"]])
(require
  '[reloaded.repl :as repl :refer [start stop go reset]]
  '[guadalete.systems.core :refer [dev-system]]
  '[environ.boot :refer [environ]]
  '[system.boot :refer [system run]])

(deftask dev
         "Run a restartable system in the r3pl."
         []
         (comp
           (environ :env {:config-file "resources/config.edn"})
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
