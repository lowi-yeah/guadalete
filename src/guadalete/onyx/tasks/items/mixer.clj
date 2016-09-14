(ns guadalete.onyx.tasks.items.mixer
    (:require
      [taoensso.timbre :as log]
      [schema.core :as s]
      [onyx.schema :as os]
      [clojurewerkz.statistiker.statistics :as statistics]
      [clj-uuid :as uuid]
      [onyx.peer.operation :refer [kw->fn]]
      [guadalete.config.onyx :refer [onyx-defaults]]
      [guadalete.onyx.tasks.identity :refer [identity-task log-task dissoc-task]]))

(s/defn in
        [{:keys [name channel] :as attributes}]
        (identity-task name)
        ;(log-task name (str "mixer/in-" channel))
        )

(defn mix* [mixin-fn-key state segment]
      (let [funktion (kw->fn mixin-fn-key)
            values (->> (vals state)
                        (map #(get % :data))
                        (into []))
            mixed(if (not-empty values)
                  (apply funktion values)
                  0)]
           (assoc segment :value mixed)))

(defn inject-state
      [{:keys [onyx.core/windows-state onyx.core/params]} _lifecycle]
      (let [state (-> @windows-state (first) (get-in [:state 1]))]
           {:onyx.core/params (conj params state)}))

(defn inject-mixin-fn
      "Injects the mixin function (@see functions above)."
      [{:keys [onyx.core/task-map]} lifecycle]
      {:onyx.core/params [(:mixer/fn task-map)]})

(def lifecycle-calls
  {:lifecycle/before-batch      inject-state
   :lifecycle/before-task-start inject-mixin-fn})


(s/defn mix!
        [{:keys [name mixin-fn] :as attributes}]
        (log/debug "TASK | mixer/mix!" attributes)
        (let [window-id (keyword "window-" (str (uuid/v1)))
              task-map (merge
                         (onyx-defaults)
                         {:onyx/name           name
                          :onyx/fn             ::mix*
                          :onyx/type           :function
                          :onyx/uniqueness-key :at
                          :onyx/doc            "Mixes two colors according to the mixin-fn "
                          :mixer/fn            mixin-fn
                          })
              windows [{:window/id          window-id
                        :window/task        name
                        :window/type        :global
                        :window/aggregation :guadalete.onyx.windowing/map-latest
                        :window/window-key  :at
                        :map-key            :id}]

              lifecycles [{:lifecycle/task  name
                           :lifecycle/calls ::lifecycle-calls}]
              task {:task   {:task-map   task-map
                             :windows    windows
                             :lifecycles lifecycles}
                    :schema {:task-map   os/TaskMap
                             :windows    [os/Window]
                             :lifecycles [os/Lifecycle]}}]
             task))


(defn out
      "Returns the task to be excuted by the out-node of a mixer.
      By now it does nothing…"
      [{:keys [name]}]
      (dissoc-task name [:id :data]))

;//         _     _         __              _   _
;//   _ __ (_)_ ___)_ _    / _|_  _ _ _  __| |_(_)___ _ _  ___
;//  | '  \| \ \ / | ' \  |  _| || | ' \/ _|  _| / _ \ ' \(_-<
;//  |_|_|_|_/_\_\_|_||_| |_|  \_,_|_||_\__|\__|_\___/_||_/__/
;//
(s/defn avg
        "Mixin function which averages the incoming signals"
        [& numbers]
        ;(log/debug "mix/avg" numbers (statistics/mean numbers))
        (statistics/mean numbers))

(s/defn product
        "Mixin function which averages the incoming signals"
        [& numbers]
        ;(log/debug "mix/avg" numbers (statistics/mean numbers))
        (statistics/product numbers))