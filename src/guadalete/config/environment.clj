(ns guadalete.config.environment
    (:require
      [environ.core :refer [env]]
      [taoensso.timbre :as log]))

(defn get-value
      [key] (get env key))

(defn get-bool
      [key] (Boolean/valueOf (get-value key)))