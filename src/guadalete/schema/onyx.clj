(ns guadalete.schema.onyx
    (:require
      [schema.core :as s]
      [onyx.schema :as os]))


(s/defschema KafkaInputTask
             {(s/required-key :kafka/topic)               s/Str
              (s/required-key :kafka/group-id)            s/Str
              (s/required-key :kafka/zookeeper)           s/Str
              (s/required-key :kafka/offset-reset)        (s/enum :smallest :largest)
              (s/required-key :kafka/force-reset?)        s/Bool
              (s/required-key :kafka/deserializer-fn)     os/NamespacedKeyword
              (s/optional-key :kafka/chan-capacity)       s/Num
              (s/optional-key :kafka/fetch-size)          s/Num
              (s/optional-key :kafka/empty-read-back-off) s/Num
              (s/optional-key :kafka/commit-interval)     s/Num})

(s/defschema KafkaOutputTask
             {(s/required-key :kafka/topic)         s/Str
              (s/required-key :kafka/zookeeper)     s/Str
              (s/required-key :kafak/serializer-fn) os/NamespacedKeyword
              (s/optional-key :kafka/request-size)  s/Num})
