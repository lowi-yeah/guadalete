(ns guadalete.onyx.tasks.util
    (:require
      [cheshire.core :as json]
      [schema.core :as s]
      [onyx.schema :as os]
      [taoensso.timbre :as log]))

(defn deserialize-message-string [bytes]
      (try
        (String. bytes "UTF-8")
        (catch Exception e
          {:error e})))

(defn deserialize-message-json [bytes]
      (try
        (json/parse-string (String. bytes "UTF-8") true)
        (catch Exception e
          {:error e})))

(defn deserialize-message-edn [bytes]
      (try
        (read-string (String. bytes "UTF-8"))
        (catch Exception e
          {:error e})))

(defn serialize-message-json [segment]
      (.getBytes (json/generate-string segment)))

(defn serialize-message-edn [segment]
      (.getBytes segment))

(defn serialize-message-string [segment]
      (.getBytes segment))
