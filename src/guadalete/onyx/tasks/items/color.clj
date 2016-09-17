(ns guadalete.onyx.tasks.items.color
    (:require
      [taoensso.timbre :as log]
      [schema.core :as s]
      [onyx.schema :as os]
      [clj-time.core :as t]
      [clj-uuid :as uuid]
      [guadalete.config.onyx :refer [onyx-defaults]]
      [guadalete.onyx.tasks.identity :refer [identity-task log-task]]))

;//   _                   _
;//  (_)_ _  __ ___ _ __ (_)_ _  __ _
;//  | | ' \/ _/ _ \ '  \| | ' \/ _` |
;//  |_|_||_\__\___/_|_|_|_|_||_\__, |
;//                             |___/

(defn inject-channel
      "Injects the color channel as a parameter into ::assoc-channel."
      [{:keys [onyx.core/task-map]} lifecycle]
      {:onyx.core/params [(:color/channel task-map)]})

(def in-color-lifecycle-calls
  {:lifecycle/before-task-start inject-channel})

(defn assoc-channel [channel segment]
      (assoc segment :channel channel))

(s/defn in
        [{:keys [name channel] :as attributes}]
        (let [task-map (merge
                         (onyx-defaults)
                         {:onyx/name           name
                          :onyx/fn             ::assoc-channel
                          :onyx/type           :function
                          :onyx/uniqueness-key :at
                          :onyx/doc            "Assocs the color channel with each segment."
                          :color/channel       channel})
              lifecycles [{:lifecycle/task  name
                           :lifecycle/calls ::in-color-lifecycle-calls}]]
             {:task   {:task-map   task-map
                       :lifecycles lifecycles}
              :schema {:task-map   os/TaskMap
                       :lifecycles [os/Lifecycle]}}))

;//   _         _    _
;//  (_)_ _  __(_)__| |___
;//  | | ' \(_-< / _` / -_)
;//  |_|_||_/__/_\__,_\___|
;//
(defn color-aggregation-fn-init [window] {})
(defn color-aggregation-fn [window state segment] segment)
(defn color-super-aggregation [window state-1 state-2] (into state-1 state-2))
(defn color-aggregation-apply-log [window state v]
      (let [{:keys [channel value at] :as segment} v]
           (clojure.core/assoc state channel {:value value :at at})))
(def map-color
  {:aggregation/init                 color-aggregation-fn-init
   :aggregation/create-state-update  color-aggregation-fn
   :aggregation/apply-state-update   color-aggregation-apply-log
   :aggregation/super-aggregation-fn color-super-aggregation})

(defn inject-state
      [{:keys [onyx.core/windows-state onyx.core/params]} _lifecycle]
      (let [state (-> @windows-state (first) (get-in [:state 1]))]
           {:onyx.core/params (conj params state)}))

(def color-lifecycle-calls
  {:lifecycle/before-batch inject-state})

(defn make-color [state segment]
      (let [color (-> {:brightness (or (get-in state [:brightness :data]) 0)
                       :saturation (or (get-in state [:saturation :data]) 0)
                       :hue        (or (get-in state [:hue :data]) 0)}
                      (assoc (:channel segment) (:data segment)))]
           (merge segment color)))

(s/defn inner
        [{:keys [name] :as attributes}]
        (log/debug "TASK | color/inner" attributes)
        (let [window-id (keyword "window-" (subs (str (uuid/v1)) 1))

              task-map (merge
                         (onyx-defaults)
                         {:onyx/name           name
                          :onyx/fn             ::make-color
                          :onyx/type           :function
                          :onyx/uniqueness-key :at
                          :onyx/doc            "Logs the color to the console for debugging"})

              windows [{:window/id          window-id
                        :window/task        name
                        :window/type        :global
                        :window/aggregation ::map-color
                        :window/window-key  :at
                        :map-key            :id}]

              lifecycles [{:lifecycle/task  name
                           :lifecycle/calls ::color-lifecycle-calls}]]
             {:task   {:task-map   task-map
                       :windows    windows
                       :lifecycles lifecycles}
              :schema {:task-map   os/TaskMap
                       :windows    [os/Window]
                       :lifecycles [os/Lifecycle]}}))

;//            _            _
;//   ___ _  _| |_ __ _ ___(_)_ _  __ _
;//  / _ \ || |  _/ _` / _ \ | ' \/ _` |
;//  \___/\_,_|\__\__, \___/_|_||_\__, |
;//               |___/           |___/
(s/defn out
        [{:keys [name]}]
        ;(identity-task name)
        (log-task name (str "color/out")))

