(ns guadalete.onyx.tasks.items.light
    (:require
      [taoensso.timbre :as log]
      [schema.core :as s]
      [onyx.schema :as os]
      [guadalete.config.onyx :refer [onyx-defaults]]
      [guadalete.onyx.tasks.async :as async]))

(s/defn in
        [{:keys [name] :as light}]
        (log/debug "light/in!!" light)
        (async/publish-task name))