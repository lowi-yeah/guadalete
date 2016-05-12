(ns guadalete.utils.util
    (:require
      [taoensso.timbre :refer (tracef debugf infof)]
      [cheshire.core :refer :all]
      [schema.core :as s]))

(defn pretty
      "Returns a prettyprinted JSON representation of the argument"
      [argument]
      (generate-string argument {:pretty true}))

(defn in?
      "true if coll contains elm"
      [coll elm]
      (some #(= elm %) coll))

(defn mappify
      "Generic convenience function for converting a collection into a map."
      [map-key collection]
      (into {} (map (fn [x] {(name (get x map-key)) x}) collection)))