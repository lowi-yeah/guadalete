(ns guadalete.schema.core
    (:require
      [schema.utils :as utils]
      [schema.core :as s]
      [schema.coerce :as coerce]
      [schema.macros :as macros]))

(s/defschema Vec2
             (s/conditional
               map? {:x s/Num
                     :y s/Num}
               :else [s/Num]))

;//   _ _      _
;//  | (_)_ _ | |_____
;//  | | | ' \| / (_-<
;//  |_|_|_||_|_\_\__/
;//
(defn in-link? [link]
      (or (= :in (:direction link))
          (= "in" (:direction link))))

(def LinkReference
  "A reference for looking up a Link"
  (s/conditional keyword? (s/eq :mouse)
                 :else {:scene-id s/Str
                        :node-id  s/Str
                        :id       s/Str}))

;; IN
;; ********************************
(def ColorInLink
  "A link accepting colors as input"
  {:id                        s/Str
   (s/optional-key :scene-id) s/Str
   (s/optional-key :node-id)  s/Str
   :accepts                   (s/eq :color)
   :direction                 (s/eq :in)
   :index                     s/Num
   (s/optional-key :name)     s/Str})

(def ValueInLink
  "A link accepting values as input"
  {:id                        s/Str
   (s/optional-key :scene-id) s/Str
   (s/optional-key :node-id)  s/Str
   :accepts                   (s/eq :value)
   :direction                 (s/eq :in)
   :index                     s/Num
   (s/optional-key :type)     s/Str
   (s/optional-key :channel)  s/Str
   (s/optional-key :name)     s/Str})


;; OUT
;; ********************************
(def ColorOutLink
  "A link emitting a color"
  {:id                        s/Str
   (s/optional-key :scene-id) s/Str
   (s/optional-key :node-id)  s/Str
   :emits                     (s/eq :color)
   :direction                 (s/eq :out)
   :index                     s/Num
   (s/optional-key :name)     s/Str})

(def ValueOutLink
  "A link emitting values"
  {:id                        s/Str
   (s/optional-key :scene-id) s/Str
   (s/optional-key :node-id)  s/Str
   :emits                     (s/eq :value)
   :direction                 (s/eq :out)
   :index                     s/Num
   (s/optional-key :name)     s/Str})

(s/defschema InLink
             (s/conditional
               #(or
                 (= (:accepts %) "color")
                 (= (:accepts %) :color)) ColorInLink
               :else ValueInLink))

(s/defschema OutLink
             (s/conditional
               #(or
                 (= (:emits %) "color")
                 (= (:emits %) :color)) ColorOutLink
               :else ValueOutLink))

(s/defschema Link
             (s/conditional in-link? InLink :else OutLink))

(s/defschema NodeData
             {:room-id  s/Str
              :scene-id s/Str
              :ilk      (s/enum :signal :color :mixer :light)
              :position Vec2})

(s/defschema NodeReference
             {:scene-id s/Str
              :id       s/Str
              :type     (s/enum :node)
              :position Vec2})

(s/defschema Node
             {:id       s/Str
              :ilk      (s/enum :signal :color :mixer :light)
              :item-id  s/Str
              :position Vec2
              :links    [Link]})

(s/defschema Nodes
             {s/Keyword Node})


;//    __ _
;//   / _| |_____ __ __
;//  |  _| / _ \ V  V /
;//  |_| |_\___/\_/\_/
;//
(s/defschema FlowReference
             "A flow between two pd nodes.
             (Between links of two nodes, to be more precise.)"
             {:from                    LinkReference
              :to                      LinkReference
              (s/optional-key :id)     s/Str
              (s/optional-key :valid?) (s/enum :valid :invalid)})

(s/defschema FlowReferences
             {s/Keyword s/Any})

(s/defschema ValueFlow
             "A schema for flows between value links"
             {:from                ValueOutLink
              :to                  ValueInLink
              (s/optional-key :id) s/Str})

(s/defschema ColorFlow
             "A schema for flows between value links"
             {:from                ColorOutLink
              :to                  ColorInLink
              (s/optional-key :id) s/Str})

(s/defschema Flow
             "An assembled flow between two pd nodes.
             In this context, assembled means that the actual links have been loaded,
             instead of just their reference ids."
             ;; i'd like to use an enum here, but validation always fails when I do so…
             ;(s/enum ValueFlow ColorFlow)
             (s/conditional
               #(or (= (-> % (get :from) (get :emits)) "value")
                    (= (-> % (get :from) (get :emits)) :value)) ValueFlow
               :else ColorFlow))


(s/defschema Rooms
             {s/Str s/Any})                                 ; map id->Room

(s/defschema Light
             {:room-id      s/Str
              :id           s/Str
              :name         s/Str
              :num-channels s/Num
              :channels     [[s/Num]]
              :color        {:brightness                  s/Num
                             (s/optional-key :saturation) s/Num
                             (s/optional-key :hue)        s/Num}
              :transport    (s/enum :dmx :mqtt)})

(s/defschema Lights
             {s/Str Light})

(s/defschema Scene
             "Scheme definition for a Scene"
             {:id          s/Str
              :name        s/Str
              :room-id     s/Str
              :mode        (s/enum :none :pan :link)        ; flag used for interacting with the gui, indicates wthere the scene is being panned or whether a link is being created
              :translation Vec2                             ; offset vector (pan) for rendering
              :nodes       Nodes
              :flows       FlowReferences
              :on?         s/Bool})

(s/defschema Scenes
             {s/Str Scene})


(s/defschema Signal
             {:name      s/Str
              :type      s/Str
              :id        s/Str
              :accepted? s/Bool})

(s/defschema Signals
             {s/Str Signal})

(s/defschema Color
             {:id         s/Str
              :color-type (s/enum :v :sv :hsv)
              :brightness s/Num})

(s/defschema Colors
             {s/Str Color})

(s/defschema Mixer
             {:id       s/Str
              :mixin-fn s/Keyword})

(s/defschema Mixers
             {s/Str Mixer})

;//                     _
;//   __ ___ ___ _ _ ____)___ _ _
;//  / _/ _ \ -_) '_(_-< / _ \ ' \
;//  \__\___\___|_| /__/_\___/_||_|
;//
(def coerce-light
  (coerce/coercer Light coerce/json-coercion-matcher))

(def coerce-mixer
  (coerce/coercer Mixer coerce/json-coercion-matcher))

(def coerce-signal
  (coerce/coercer Signal coerce/json-coercion-matcher))

(def coerce-color
  (coerce/coercer Color coerce/json-coercion-matcher))

(def coerce-scenes
  (coerce/coercer [Scene] coerce/json-coercion-matcher))


