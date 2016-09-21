(ns guadalete.onyx.tasks.items.light
    (:require
      [taoensso.timbre :as log]
      [schema.core :as s]
      [onyx.schema :as os]
      [thi.ng.math.core :as math]
      [thi.ng.color.core :as col]
      [guadalete.schema.core :as gs]
      [guadalete.config.onyx :refer [onyx-defaults]]
      [guadalete.config.core :as config]
      [guadalete.onyx.tasks.async :as async]
      [guadalete.onyx.tasks.mqtt :as mqtt]
      [guadalete.onyx.tasks.kafka :as kafka-tasks]
      [guadalete.onyx.tasks.identity :refer [log-task]]))

(s/defn hsv->rgb
        "Maps a given Color"
        [type :- (s/enum :v :sv :hsv)
         {:keys [brightness saturation hue] :as color} :- gs/SimpleColor]
        (let [
              hsva* (col/hsva hue saturation brightness 1)
              rgba* (col/as-rgba hsva*)
              b (int (math/map-interval-clamped brightness 0 1 0 255))
              s (int (math/map-interval-clamped saturation 0 1 0 255))
              r (int (math/map-interval-clamped (:r rgba*) 0 1 0 255))
              g (int (math/map-interval-clamped (:g rgba*) 0 1 0 255))
              b (int (math/map-interval-clamped (:b rgba*) 0 1 0 255))]
             (condp = type
                    :v (str b)
                    :sv (str [b s])
                    :hsv (str [r g b]))))

(s/defn prepare-mqtt
        [id :- s/Str
         color-type :- (s/enum :v :sv :hsv)
         segment]
        {:message (hsv->rgb color-type segment)
         :key     id})

(s/defn ^:always-validate prepare-for-transport
        "Task function for :light/in."
        [id :- s/Str
         transport :- (s/enum :dmx :mqtt)
         color-type :- (s/enum :v :sv :hsv)
         segment]

        (condp = transport
               :mqtt (prepare-mqtt id color-type segment)
               :dmx segment))

(defn inject-attributes
      "Injects the transport type (or :dmx :mqtt)"
      [{:keys [onyx.core/task-map]} lifecycle]
      {:onyx.core/params [(:light/id task-map)
                          (:light/transport task-map)
                          (:light/color-type task-map)]})

(def light-in-lifecycle-calls
  {:lifecycle/before-batch inject-attributes})

(s/defn in
        [{:keys [name transport color-type light-id] :as attributes}]
        (let [task-map (merge
                         (onyx-defaults)
                         {:onyx/name           name
                          :onyx/fn             ::prepare-for-transport
                          :onyx/type           :function
                          :onyx/uniqueness-key :at
                          :onyx/doc            "Recieves color messages and tranforms them accoring to the lights transport type."
                          :light/transport     transport
                          :light/color-type    color-type
                          :light/id            light-id})
              lifecycles [{:lifecycle/task  name
                           :lifecycle/calls ::light-in-lifecycle-calls}]]
             {:task   {:task-map   task-map
                       :lifecycles lifecycles}
              :schema {:task-map   os/TaskMap
                       :lifecycles [os/Lifecycle]}}))


(s/defn out
        [light]
        (log/debug "light/out!" light)
        (condp = (:transport light)
               :dmx (async/publish-task (:name light))
               :mqtt (do
                       (log/debug "transport MQTT" light)
                       (kafka-tasks/light-producer (:name light)))))