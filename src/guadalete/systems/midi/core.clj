(ns guadalete.systems.midi.core
    (:require
      [clojure.core.async :refer [go go-loop <! >! chan]]
      [com.stuartsierra.component :as component]
      [taoensso.timbre :as log]
      [guadalete.utils.util :refer [in?]]
      [overtone.midi :refer :all]
      ))


(defrecord Midi [port]
           component/Lifecycle
           (start [component]
                  (log/info "**************** Starting MIDI server ***************")
                  (log/debug "midi port" (str port))
                  (try
                    (let []
                         component)
                    (catch Exception e (str "caught exception: " (.getMessage e)))))

           (stop [component]
                 (log/info "Stopping MIDI component")
                 component))

(defn new-midi [config] (map->Midi config))

