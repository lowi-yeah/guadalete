;; System which holds care.asyn channels for use in onyx
(ns guadalete.systems.async
    (:require
      [clojure.core.async :refer [chan pub sub >!! <!! close!]]
      [com.stuartsierra.component :as component]
      [onyx.api]
      [taoensso.timbre :as log]
      ))

(defrecord Async [topics]
           component/Lifecycle
           (start [component]
                  (log/info "**************** Starting Async component ***************")
                  (log/debug "***** topics" topics)
                  (let [channels (->> topics
                                      (map
                                        (fn [topic] [topic (chan)]))
                                      (into {}))]
                       (assoc component :channels channels)))

           (stop [component]
                 (log/info "Stopping Async component")
                 (dissoc component :channels)))

(defn new-async [config]
      (map->Async config))


