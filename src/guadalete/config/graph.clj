(ns guadalete.config.graph
    (:require
      [taoensso.timbre :as log]
      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.onyx.tasks.mqtt :as mqtt]
      [guadalete.config.core :as config]))

(s/defn ^:always-validate task*
        [ns* :- s/Keyword
         fn* :- s/Keyword]
        (keyword (str "guadalete.onyx.tasks.items." (name ns*)) (name fn*)))

(s/defn ^:always-validate make-id :- s/Keyword
        [item-id :- s/Str
         link-id :- s/Str]
        (keyword (str item-id "-" link-id)))

(defn- light-in-attributes
       [type node item]
       {:type       type
        :name       (make-id (:id item) (name type))
        :transport  (:transport item)
        :light-id   (:id item)
        :task       (task* :light :in)
        :color-type (:type item)})

(defn- light-sink-attributes
       [type node item]
       (log/debug "light-sink-attributes | type" type)
       (let [base {:type       type
                   :name       (make-id (:id item) (name type))
                   :transport  (:transport item)
                   :task       (task* :light :out)
                   :color-fn   :guadalete.onyx.tasks.items.light/hsv->rgb
                   :color-type (:type item)}
             transport-specific (condp = (:transport item)
                                       :mqtt {:mqtt-id   (:id item)
                                              :client-id (:id node)
                                              :broker    (:mqtt-broker (config/mqtt))
                                              :topic     (mqtt/make-topic (:id item) type)}
                                       :dmx {:channels (:channels :item)})]
            (merge base transport-specific)))


(s/defn attributes-for-type
        "Look up the attributes reqired to perform the task for a given ubergraph-node-type"
        [type node item]
        (condp = type
               :signal/out {:type      type
                            :name      (make-id (:id item) (name type))
                            :task      (task* :signal :in)
                            :signal-id (:id item)
                            :id        (:id node)
                            }

               :mixer/in-0 {:type    type
                            :name    (make-id (:id item) (name type))
                            :task    (task* :mixer :in)
                            :channel 0}

               :mixer/in-1 {:type    type
                            :name    (make-id (:id item) (name type))
                            :task    (task* :mixer :in)
                            :channel 1}

               :mixer/inner {:type     type
                             :name     (make-id (:id item) (name type))
                             :task     (task* :mixer :mix!)
                             :mixin-fn (keyword "guadalete.onyx.tasks.items.mixer" (name (:mixin-fn item)))}

               :mixer/out {:type type
                           :name (make-id (:id item) (name type))
                           :task (task* :mixer :out)}

               :color/brightness {:type    type
                                  :name    (make-id (:id item) (name type))
                                  :channel (keyword (name type))
                                  :task    (task* :color :in)}

               :color/saturation {:type    type
                                  :name    (make-id (:id item) (name type))
                                  :channel (keyword (name type))
                                  :task    (task* :color :in)}

               :color/hue {:type    type
                           :name    (make-id (:id item) (name type))
                           :channel (keyword (name type))
                           :task    (task* :color :in)}

               :color/inner {:type type
                             :name (make-id (:id item) (name type))
                             :task (task* :color :inner)}

               :color/out {:type type
                           :name (make-id (:id item) (name type))
                           :task (task* :color :out)}

               :light/in (light-in-attributes type node item)
               :light/sink (light-sink-attributes type node item)))
