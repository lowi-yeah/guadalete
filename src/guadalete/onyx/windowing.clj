(ns guadalete.onyx.windowing
    (:require [taoensso.timbre :refer [debug info error warn trace fatal]])
    (:refer-clojure :exclude [min max count conj]))


(defn latest-aggregation-fn-init [window]
      {})

(defn latest-aggregation-fn [window state segment]
      segment)

; not sure what this one does yet…
(defn latest-super-aggregation [window state-1 state-2]
      (into state-1 state-2))

(defn latest-aggregation-apply-log [window state v]
      (let [{:keys [id data at mixed] :as segment} v]
           (clojure.core/assoc state (keyword id) {:data data :at at})))

(def map-latest
  {:aggregation/init                 latest-aggregation-fn-init
   :aggregation/create-state-update  latest-aggregation-fn
   :aggregation/apply-state-update   latest-aggregation-apply-log
   :aggregation/super-aggregation-fn latest-super-aggregation})