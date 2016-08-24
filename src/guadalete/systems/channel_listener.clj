(ns guadalete.systems.channel-listener
    (:require
      [clojure.string :as str]
      [clojure.core.async :refer [>! >!! <! go go-loop chan alts! close!]]
      [cheshire.core :refer [generate-string parse-string]]
      [com.stuartsierra.component :as component]
      [guadalete.jobs.state :as state]
      [taoensso.timbre :as log]))

(defn- run!
       [channels]
       (log/debug "ChannelListener.run!")

       (let [stop-channel (chan)]
            (go-loop []
                     (let [[v ch] (alts! (conj channels stop-channel))]
                          (when-not (= ch stop-channel)
                                    (log/debug v)
                                    (recur))))
            stop-channel))

(defrecord ChannelListener []
           component/Lifecycle
           (start [component]
                  (let [
                        ;sine (state/subscribe :signal/value "sine")
                        ;slowsine (state/subscribe :signal/value "slowsine")
                        ;quicksine-channel (state/subscribe :signal/value "quicksine")
                        ;stop-channel (run! [sine-channel quicksine-channel])

                        channels (state/get-channels)

                        _ (log/debug "channels" channels)

                        stop-channel (run! channels)
                        ]
                       (assoc component :stop stop-channel)))

           (stop [component]
                 (let [stop-ch (:stop component)]
                      (log/info "Stopping component: ChannelListener")
                      (go (>! stop-ch "halt!"))
                      (dissoc component :stop))))

(defn new-channel-listener []
      (map->ChannelListener {}))
