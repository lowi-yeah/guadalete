(ns guadalete.schema.core
    (:require [schema.utils :as utils]
      [schema.core :as s]
      [onyx.schema :as os]
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
(s/defschema ArtnetUniverse {:universe       s/Str
                             :id             s/Str
                             :ip             s/Str
                             :numDmxChannels s/Num
                             :serverPort     s/Num})

(s/defschema Vec2
             {:x s/Num
              :y s/Num})


;//     _     _
;//    (_)___| |__
;//    | / _ \ '_ \
;//   _/ \___/_.__/
;//  |__/
(s/defschema JobMap
             {s/Str os/Job})


;//    __ _
;//   / _| |_____ __ __
;//  |  _| / _ \ V  V /
;//  |_| |_\___/\_/\_/
;//

(s/defschema Item s/Any)

;(s/defschema Link
;             {:id       s/Str
;              :scene-id s/Str
;              :node-id  s/Str
;              :item     Item})

(s/defschema Flow
             {:from Item
              :to   Item
              :id   s/Str})

(s/defschema FlowMap
             {s/Keyword [Flow]})


;//                      _
;//   __ _ _ _ __ _ _ __| |_
;//  / _` | '_/ _` | '_ \ ' \
;//  \__, |_| \__,_| .__/_||_|
;//  |___/         |_|

;(s/defschema GraphSchema
;             {:node-map s/Any
;              :allow-parallel? s/Bool
;              :undirected? s/Bool
;              :attrs s/Any
;              :cached-hash s/Any
;              })
;//   _ _ _ _ _ _ _ _
;//  (_)_)_)_)_)_)_)_)

(s/defschema Link
             {:id                    s/Str
              :index                 s/Num
              :direction             (s/enum "out" "in")
              :ilk                   (s/enum "value" "color")
              (s/optional-key :type) s/Str
              (s/optional-key :name) s/Str
              })

(s/defschema Node
             {:id       s/Str
              :item-id  s/Str
              :ilk      s/Str
              :links    {s/Keyword Link}
              ;:links    s/Any
              :position Vec2
              })

(s/defschema LinkReference
             {:id       s/Str
              :node-id  s/Str
              :scene-id s/Str})

(s/defschema FlowReference
             {:id   s/Str
              :from LinkReference
              :to   LinkReference})

(s/defschema Scene
             {:flows       {s/Keyword FlowReference}
              :id          s/Str
              :mode        (s/eq "none")
              :name        s/Str
              :nodes       {s/Keyword Node}
              ;:nodes       {s/Keyword s/Any}
              :on?         s/Bool
              :room-id     s/Str
              :translation Vec2
              })
(s/defschema Room s/Any)
(s/defschema Light s/Any)
(s/defschema Signal s/Any)

