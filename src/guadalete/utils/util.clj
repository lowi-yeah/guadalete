(ns guadalete.utils.util
    (:require
      [taoensso.timbre :refer (tracef debugf infof)]
      [cheshire.core :refer :all]
      [taoensso.timbre :as log]
      [schema.core :as s]
      [clj-time.core :as t]
      [clj-time.coerce :as tc]
      [schema.core :as s]))

(defn deep-merge
      "Deep merge two maps"
      [& values]
      (if (every? map? values)
        (apply merge-with deep-merge values)
        (last values)))

(defn pretty
      "Returns a prettyprinted JSON representation of the argument"
      [argument]
      (generate-string argument {:pretty true}))

(defn in?
      "true if coll contains elm"
      [coll elm]
      (some #(= elm %) coll))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn kw* [something]
      (keyword something))

(defn mappify
      "Generic convenience function for converting a collection into a map."
      [map-key collection]
      (into {} (map (fn [x] {(name (get x map-key)) x}) collection)))

(defn now
      "milliseconds since Unix epoch"
      []
      (tc/to-long (t/now)))

(defn merge-keywords
      "merges the names of two keywords [:key-0 :key-1] into one namespaced keyword :key-0/key-1"
      [key-0 key-1]
      ;(keyword (str (name key-0) "-" (name key-1)))
      (keyword (name key-0) (name key-1))
      )

(defn validate!
      [schema data]
      (try
        (log/debug "validate" data)
        (log/debug "schema" schema)
        (s/validate schema data)
        (log/debug "**** VALID! ****")
        (catch Exception e
          (log/error "ERROR" e))))