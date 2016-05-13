(ns guadalete.core
    (:gen-class)
    (:require
      [reloaded.repl :refer [init start stop reset]]
      [guadalete.systems.core :refer [prod-system]]
      [onyx.plugin.kafka]
      [onyx.plugin.redis]
      ))

(defn -main
      "Start a production system."
      [& args]
      (let [system (or (first args) #'prod-system)]
           (init system)
           (start)))
