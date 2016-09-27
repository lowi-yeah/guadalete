(ns guadalete.onyx.tasks.mqtt
    "Tasks for writing data to MQTT"
    (:require [clojure
               [string :refer [capitalize trim]]
               [walk :refer [postwalk]]]
      [taoensso.timbre :as log]
      ;[cheshire.core :refer [generate-string]]
      [clojurewerkz.machine-head.client :as mh]
      [guadalete.config.onyx :refer [onyx-defaults]]
      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.schema.core :as gs]))


(s/defschema MqttOutputTask
             {:mqtt/topic     s/Str
              :mqtt/broker    s/Str
              :mqtt/client-id s/Str})


(s/defn make-topic :- s/Str
        [item-id :- s/Str
         type :- (s/enum :light/in)]
        (str "/lght/o/" (trim item-id)))


(s/defn ^:always-validate publish
        [task-name :- s/Keyword
         topic :- s/Str
         client-id :- s/Str
         broker :- s/Str
         color-fn :- s/Keyword
         color-type :- s/Keyword]
        (log/debug "mqtt publish" task-name topic client-id broker color-fn color-type)
        {:task   {:task-map   (merge (onyx-defaults)
                                     {:onyx/name        task-name
                                      :onyx/plugin      :guadalete.onyx.plugin.mqtt/publish
                                      ;:onyx/plugin      :guadalete.onyx.plugin.mock-mqtt/publish
                                      :onyx/type        :output
                                      :onyx/medium      :mqtt
                                      :onyx/doc         "Publishes segment to mqtt"
                                      :mqtt/topic       topic
                                      :mqtt/broker      broker
                                      :mqtt/client-id   client-id
                                      :color/mapping-fn color-fn
                                      :color/type       color-type})
                  :lifecycles [{:lifecycle/task  task-name
                                :lifecycle/calls :guadalete.onyx.plugin.mqtt/publish-calls
                                ;:lifecycle/calls :guadalete.onyx.plugin.mock-mqtt/publish-calls
                                }]}
         :schema {:task-map   (merge os/TaskMap MqttOutputTask)
                  :lifecycles [os/Lifecycle]}})