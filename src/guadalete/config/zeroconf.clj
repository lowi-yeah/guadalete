(ns guadalete.config.zeroconf
    (:require
      [com.stuartsierra.component :as component]
      [clj-kafka.admin :as admin]
      [taoensso.timbre :as log]
      [cassiel.zeroconf.client :as zeroconf]))

(defn- examine* [listener retry-count]
       (if (> retry-count 1)
         {}
         (let [_ (Thread/sleep (* 1000 retry-count))
               description (zeroconf/examine listener)]
              (if (empty? description)
                (do
                  (examine* listener (inc retry-count)))
                ;; not empty
                (do
                  (zeroconf/close listener)
                  (-> description (first) (second)))))))

(defn- examine [service-name listener]
       [service-name (examine* listener 0)])

(defn discover [services]
      (let [listeners (doall (mapv (fn [[service-name service-id]]
                                       [service-name (zeroconf/listen service-id)])
                                   services))]
           (Thread/sleep 1000)
           (doall
             (->> listeners
                  (map (fn [[service-name listener]]
                           (examine service-name listener)))
                  (into {})))))