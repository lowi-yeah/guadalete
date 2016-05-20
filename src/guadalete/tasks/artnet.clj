(ns guadalete.tasks.artnet
    "Behaviors send DMX packages to artnet"
    (:require [clojure
               [string :refer [capitalize trim]]
               [walk :refer [postwalk]]]
      [taoensso.timbre :as log]
      [cheshire.core :refer [generate-string]]
      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.schema.core :as gs])

    (:import
      (artnet4j ArtNet)
      (artnet4j ArtNetNodeDiscovery)
      (artnet4j ArtNetNode)
      (artnet4j NodeStyle)
      (artnet4j DmxUniverse)
      (artnet4j DmxUniverseConfig)
      (artnet4j.packets PacketType)
      (artnet4j.packets ArtPollReplyPacket)
      ))


;ArtDmxPacket dmxPacket = dmx.getPacket(0);
;artnet.unicastPacket(dmxPacket, artNetNode.getIPAddress());
;sequenceID++;


(defn write-to-artnet [event lifecycle]
      (let [artnet (:artnet event)
            sequence-id (:artnet/sequence-id event)
            universe (:artnet/universe event)
            ip-address (-> universe
                           (.getNode)
                           (.getIPAddress))
            batch (:onyx.core/batch event)]

           ;(doseq [i (range 512)] (.setChannel universe i (rand-int 255)))

           (doseq [item batch]
                  (let [dmx-channel (get-in item [:message :id])
                        value (get-in item [:message :val])]
                       ;(.setChannel universe dmx-channel value)
                       (.setChannel universe 0 value)))



           (let [dmx-packet (.getPacket universe @sequence-id)]
                ;(.unicastPacket artnet dmx-packet ip-address)
                (.broadcastPacket artnet dmx-packet)
                (swap! (:artnet/sequence-id event) inc)
                {})))

(defn make-state [_event lifecycle]
      (let [artnet (ArtNet.)
            packet (ArtPollReplyPacket.)]
           :broadcast-address
           (.start artnet)
           (.setBroadCastAddress artnet (:artnet/broadcast-address lifecycle))
           (.parse packet (:artnet/config-bytes lifecycle))
           (let [
                 node-style (.getNodeStyle packet)
                 node (.createNode node-style)
                 ]
                (.extractConfig node packet)
                (let [num-dmx-channels (.getNumPorts node)
                      config (DmxUniverseConfig.)
                      ip (.getIPAddress node)]
                     (set! (.id config) "0")
                     (set! (.ip config) ip)
                     (set! (.numDmxChannels config) num-dmx-channels)
                     (set! (.serverPort config) (:artnet/server-port lifecycle))
                     {:artnet             artnet
                      :artnet/sequence-id (atom 0)
                      :artnet/universe    (DmxUniverse. node config)}
                     ))))

(defn destroy-state [event _lifecycle]
      (log/debug "destroy state" (:artnet event))
      (.stop (:artnet event))
      {}
      )

(def artnet-lifecycle
  {:lifecycle/before-task-start make-state
   :lifecycle/after-read-batch  write-to-artnet
   :lifecycle/after-task-stop   destroy-state})

(s/defn output-task
        [task-name :- s/Keyword
         {:keys [task-opts lifecycle-opts] :as opts}]
        {:task   {:task-map   (merge {:onyx/name   task-name
                                      :onyx/plugin :onyx.peer.function/function
                                      ;; We don't want to transform the data, just write it.
                                      ;; so we merely use the identity function.
                                      :onyx/fn     :clojure.core/identity
                                      :onyx/type   :output
                                      :onyx/medium :function
                                      :onyx/doc    "Does nothing (identity) but provide a place where lifecycle functions can hook into… "}
                                     task-opts
                                     )
                  :lifecycles [(merge {:lifecycle/task  task-name
                                       :lifecycle/calls :guadalete.tasks.artnet/artnet-lifecycle}
                                      lifecycle-opts)
                               ]}
         :schema {:task-map   (merge os/TaskMap gs/ArtnetUniverse)
                  :lifecycles [os/Lifecycle]}})