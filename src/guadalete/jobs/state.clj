(ns guadalete.jobs.state
    (:require
      [clojure.core.async :refer [chan pub sub >!! <!! close!]]
      [taoensso.timbre :as log]))

(def channel-capacity 1024)

(def state-atom (atom {:channels             {}
                       :publication-channels {}
                       :publications         {}
                       :subscriptions        {}}))

(defn reset []
      (reset! state-atom {:channels      {}
                          :publications  {}
                          :subscriptions {}}))

(defn publication-channel [topic]
      (let [channel (get-in @state-atom [:publication-channels topic])]
           (if channel
             channel
             (let [channel* (chan channel-capacity)
                   publication* (pub channel* :id)]
                  (swap! state-atom assoc-in [:publication-channels topic] channel*)
                  (swap! state-atom assoc-in [:publications topic] publication*)
                  channel*))))

(defn out-channel [id]
      (let [channel (get-in @state-atom [:channels id])]
           (if channel
             channel
             (let [channel* (chan channel-capacity)]
                  (swap! state-atom assoc-in [:channels id] channel*)
                  channel*))))

(defn- publication* [topic]
       (let [publication (get-in @state-atom [:publications topic])]
            (if publication
              publication
              (let [channel* (chan channel-capacity)
                    publication* (pub channel* :id)]
                   (swap! state-atom assoc-in [:publication-channels topic] channel*)
                   (swap! state-atom assoc-in [:publications topic] publication*)
                   publication*))
            )
       )



(defn subscribe [topic id]
      (let [publication (publication* topic)]
           (log/debug "subscribe to" topic id)
           (let [channel* (chan)]
                (sub publication id channel*)
                channel*)))

(defn get-channels []
      (vals (:channels @state-atom)))