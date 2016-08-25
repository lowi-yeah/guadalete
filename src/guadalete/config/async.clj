(ns guadalete.config.async
    (:require
      [guadalete.config.environment :as env]
      [taoensso.timbre :as log]))

(defn config* []
      {:topics [:sgnl/v]})
