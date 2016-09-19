(ns guadalete.onyx.plugin.mock-mqtt
    (:require
      [onyx.peer.function :as function]
      [onyx.peer.pipeline-extensions :as p-ext]
      [onyx.static.default-vals :refer [defaults arg-or-default]]
      [taoensso.timbre :refer [debug info] :as log]
      [clojurewerkz.machine-head.client :as mh]
      [cheshire.core :refer [generate-string]]))

(defn connect [event lifecycle]
      (let [broker (get-in event [:onyx.core/task-map :mqtt/broker])
            topic (get-in event [:onyx.core/task-map :mqtt/topic])
            map-fn (get-in event [:onyx.core/task-map :color/map-fn])]
           (log/debug "Considering connecting to MQTT broker." broker topic)
           (log/debug "…nope")
           {:mqtt/topic   topic
            :color/map-fn map-fn}))

;; map of lifecycle calls that are required to use this plugin
;; users will generally always have to include these in their lifecycle calls
;; when submitting the job
(def publish-calls
  {:lifecycle/before-task-start connect})

(defrecord MqttOutput []
           ;; Read batch can generally be left as is. It simply takes care of
           ;; receiving segments from the ingress task
           p-ext/Pipeline
           (read-batch
             [_ event]
             (function/read-batch event))

           (write-batch
             ;; Write the batch that was read out to your datasink.
             ;; Messages are on the leaves :tree, as :onyx/fn is called
             ;; and each incoming segment may return n segments
             [_ {:keys [onyx.core/results mqtt/connection mqtt/topic color/map-fn] :as event}]
             (doseq [msg (mapcat :leaves (:tree results))]
                    (log/debug "not publishing" (:message msg)))
             {})

           (seal-resource
             ;; Clean up any resources you opened.
             ;; If relevant, put a :done on your datasource so that
             ;; any readers will know the data sink has been sealed
             [_ {:keys [mqtt/connection]}]
             (if connection
               (mh/disconnect connection))))


;; Builder function for your output plugin.
;; Instantiates a record.
;; It is highly recommended you inject and pre-calculate frequently used data
;; from your task-map here, in order to improve the performance of your plugin
;; Extending the function below is likely good for most use cases.
(defn publish [pipeline-data]
      (->MqttOutput))
