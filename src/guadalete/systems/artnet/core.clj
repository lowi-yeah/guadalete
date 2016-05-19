;//            _            _                _
;//   __ _ _ _| |_ _ _  ___| |_   ____  _ ___ |_ ___ _ __
;//  / _` | '_|  _| ' \/ -_)  _| (_-< || (_-<  _/ -_) '  \
;//  \__,_|_|  \__|_||_\___|\__| /__/\_, /__/\__\___|_|_|_|
;//                                  |__/

;// Bootstraps the artnet system and discovers the nodes in the network.

(ns guadalete.systems.artnet.core
    (:require
      [clojure.core.async :refer [go go-loop <! >! chan]]
      [com.stuartsierra.component :as component]
      [taoensso.timbre :as log]
      [guadalete.utils.util :refer [in?]])
    (:import
      [artnet4j.ArtNet]
      [artnet4j.ArtNetNodeDiscovery]
      [artnet4j.events.ArtNetDiscoveryListener]
      [artnet4j.events.ArtNetServerListener]
      [artnet4j.ArtNetNode]
      [artnet4j.packets.PacketType]
      [artnet4j.packets.ArtPollReplyPacket]
      [artnet4j.NodeStyle]
      [artnet4j.DmxUniverse]
      [artnet4j.DmxUniverseConfig]
      [artnet4j.packets.ArtNetPacketParser]
      (sun.misc BASE64Decoder)
      (java.util Arrays)
      ))


(defn test-array
      [t]
      (let [check (type (t []))]
           (fn [arg] (instance? check arg))))

(def byte-array?
  (test-array byte-array))

(defn- make-server-listener
       "Instead of creating a ArtNetDiscoveryListener, which would usually be used to discover artnet-nodes within the network,
       a server listener is being used. The reason is, that the ArtNetDiscoveryListener returns a fully initialized ArtnetNode,
       which cannot be transferred into onyx. The reason is, that an ArtnetNode is not freezable (sez the Exception - whatever this might mean…)
       and onyx does not like this. Instead the server listner emits the ArtPollReplyPacket directly, and the actual ArtnetNode is
       then created based on that information from within onyx."
       [config-promise]
       (reify
         artnet4j.events.ArtNetServerListener
         (artNetPacketReceived
           [this packet]
           (let [type (.getType packet)]
                (when (= type artnet4j.packets.PacketType/ART_POLL_REPLY)
                      ;iPAddress
                      ;subSwitch
                      ;oemCode
                      ;nodeStatus
                      ;shortName
                      ;longName
                      ;ports
                      ;numPorts
                      ;reportCode
                      ;dmxIns
                      ;dmxOuts
                      (deliver config-promise (byte-array (.getData packet))))))

         (artNetPacketBroadcasted [this packet] ())
         (artNetPacketUnicasted [this packet] ())
         (artNetServerStarted [this server] ())
         (artNetServerStopped [this server] ())))

(defrecord Artnet [node-port poll-interval broadcast-address server-port]
           component/Lifecycle
           (start [component]
                  (log/info "**************** Starting Artnet component ***************")
                  (let [artnet (artnet4j.ArtNet.)
                        node (artnet4j.ArtNetNode.)
                        config-promise (promise)
                        server-listener (make-server-listener config-promise)]
                       (.start artnet)
                       (.setBroadCastAddress artnet broadcast-address)
                       (.addServerListener artnet server-listener)
                       (let [discovery (.getNodeDiscovery artnet)]
                            (.setInterval discovery poll-interval)
                            (.start discovery)
                            (while
                              (not (realized? config-promise))
                              (Thread/sleep 200))           ; wait for the promise to be delivered before proceeding
                            (.stop discovery)               ; stop when w found a node
                            (.stop artnet)               ; stop when w found a node
                            (log/debug "config-promise delivered:" @config-promise)
                            (assoc component :config-bytes @config-promise))))

           (stop [component]
                 (log/info "Stopping Artnet component")
                 (dissoc component :config-bytes)))

(defn new-artnet [config]
      (map->Artnet config))