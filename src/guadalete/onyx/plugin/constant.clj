(ns guadalete.onyx.plugin.constant
    (:require
      [clojure.core.async :refer [chan >! <! <!! go timeout]]
      [onyx.peer.function :as function]
      [onyx.peer.pipeline-extensions :as p-ext]
      [onyx.static.default-vals :refer [defaults arg-or-default]]
      [onyx.types :as t]
      [onyx.plugin.simple-input :as i]
      [onyx.plugin.utils :as u]
      [onyx.static.uuid :as uuid]
      [taoensso.timbre :refer [debug info] :as timbre]))

;; Often you will need some data in your event map for use by the plugin
;; or other lifecycle functions. Try to place these in your builder function (pipeline)
;; first if possible.
(defn inject-into-eventmap
      [event lifecycle]
      (let [task-map (:onyx.core/task-map event)
            value (:constant/value task-map)
            id (:onyx/name task-map)]

           (when-not value
                     (throw (ex-info ":constant/value not found - add it to the task map"
                                     {:task-map-keys (keys task-map)})))

           (let [pipeline (:onyx.core/pipeline event)]
                {:constant/pending-messages (:pending-messages pipeline)
                 :constant/drained?         (:drained? pipeline)
                 :constant/value            value
                 :constant/id               id}))
      )

(def reader-calls
  {:lifecycle/before-task-start inject-into-eventmap})

(defrecord ConstantInput [max-pending batch-size batch-timeout pending-messages drained?]
           p-ext/Pipeline

           (write-batch
             [_ event]
             (function/write-batch event))

           (read-batch
             [_ event]
             (let [message {:data (:constant/value event)
                            :id   (:constant/id event)
                            :at   (System/currentTimeMillis)}
                   batch (future (Thread/sleep batch-timeout)
                                 [(t/input (uuid/random-uuid) message)])]
                  {:onyx.core/batch @batch}))

           (seal-resource [_ _])

           p-ext/PipelineInput
           (ack-segment [_ _ segment-id]
                        (swap! pending-messages dissoc segment-id))

           (retry-segment
             [_ {:keys [constant/value] :as event} segment-id]
             ;; Constant messages are NOT being retried if they are not acked in time.
             ;; To achieve this, take the message out of your pending-messages atom
             (when-let [msg (get @pending-messages segment-id)]
                       (swap! pending-messages dissoc segment-id)))

           (pending?
             [_ _ segment-id]
             (get @pending-messages segment-id))

           (drained?
             [_ _]
             @drained?))

;; Builder function for your plugin. Instantiates a record.
;; It is highly recommended you inject and pre-calculate frequently used data
;; from your task-map here, in order to improve the performance of your plugin
;; Extending the function below is likely good for most use cases.
(defn input [event]
      (debug "CONSTANT/inputt" event)
      (let [task-map (:onyx.core/task-map event)
            max-pending (arg-or-default :onyx/max-pending task-map)
            batch-size (:onyx/batch-size task-map)
            batch-timeout (arg-or-default :onyx/batch-timeout task-map)
            pending-messages (atom {})
            drained? (atom false)]
           (->ConstantInput max-pending batch-size batch-timeout pending-messages drained?)))
