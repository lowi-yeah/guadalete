(ns guadalete.schema.core
    (:require [schema.utils :as utils]
      [schema.core :as s]
      ;[schema.macros :as macros]
      ))

(s/defschema Signal
             {:id          s/Str                            ; the id of the signal
              :type        (s/enum :continuous :discrete :binary) ; the type of signal
              :name        s/Str                            ; human readable name
              :description s/Str                            ; human readable description (should be brief)
              :accepted?   s/Bool                           ; accepted as legit? (flag set by administrator)
              :value       s/Num                            ; the signal value
              :updated     s/Num                            ; timestamp of last activity
              :created     s/Num                            ; timestamp of first contact
              })

(s/defschema WeatherSignalConfig
             "Schema for weather signal configuration"
             {:mqtt-broker            s/Str
              :mqtt-id                s/Str
              :parameter              (s/enum "temperature" "windSpeed" "cloudCover" "pressure" "ozone" "humidity")
              :value-update-interval  s/Num
              :config-update-interval s/Num
              })
