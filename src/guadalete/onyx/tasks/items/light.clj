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
      [guadalete.onyx.tasks.mqtt :as mqtt] [guadalete.onyx.tasks.kafka :as kafka-tasks]))

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

(s/defn in
        [light]
        (log/debug "light/in!" light)

        (condp = (:transport light)
               :dmx (async/publish-task (:name light))
               :mqtt (do
                       (log/debug "transport MQTT" light)

                       (kafka-tasks/light-producer (:name light))
                       ;(mqtt/publish (:name light) (:topic light) (:client-id light) (:broker light) (:color-fn light) (:color-type light))
                       )))