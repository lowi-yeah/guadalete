(ns guadalete.signals.util
    (:require
      [clojure.core.async :refer [go-loop chan timeout alts!]]
      [taoensso.timbre :as log]))

(defn value-topic [id] (str "sgnl/v/" id))
(defn config-topic [id] (str "sgnl/c/" id))

(defn run
       "Runs a f (fnunction without parameters) every time-in-ms."
       [f time-in-ms]
       (let [stop (chan)]
            (go-loop []
                     (let [timeout-ch (timeout time-in-ms)
                           [v ch] (alts! [timeout-ch stop])]
                          (when-not (= ch stop)
                                    (f)
                                    (recur))))
            stop))