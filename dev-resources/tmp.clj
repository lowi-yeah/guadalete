[ {
   "task-map" : {
                 "kafka/zookeeper" : "zookeeper1:2181",
                                   "onyx/plugin" : "onyx.plugin.kafka/read-messages",
                 "filter/signal-id" : "quicksine",
                 "onyx/medium" : "kafka",
                 "kafka/offset-reset" : "largest",
                 "kafka/force-reset?" : true,
                 "onyx/batch-timeout" : 1000,
                 "onyx/type" : "input",
                 "onyx/name" : "quicksine/out",
                 "kafka/topic" : "gdlt-sgnl-v",
                 "kafka/group-id" : "sgnl-fe4d4bb8-22c1-44eb-8e5f-80b5e21168bb",
                 "onyx/max-peers" : 1,
                 "onyx/min-peers" : 1,
                 "onyx/doc" : "Consumes gdlt-sgnl-v messages from Kafka",
                 "kafka/empty-read-back-off" : 50,
                 "kafka/fetch-size" : 307200,
                 "onyx/batch-size" : 10,
                 "kafka/deserializer-fn" : "guadalete.onyx.tasks.util/deserialize-message-json",
                 "kafka/wrap-with-metadata?" : false,
                 "kafka/commit-interval" : 50,
                 "kafka/chan-capacity" : 100
                 },
              "lifecycles" : [ {
                                "lifecycle/task" : "quicksine/out",
                                                 "lifecycle/calls" : "onyx.plugin.kafka/read-messages-calls"
                                } ]
   }, {
       "task-map" : {
                     "onyx/fn" : "guadalete.onyx.tasks.items.color/log-color",
                               "onyx/uniqueness-key" : "at",
                     "onyx/batch-timeout" : 1000,
                     "onyx/type" : "function",
                     "onyx/name" : "4a39db00-55c4-4f4f-937f-255b64af07ee/brightness",
                     "onyx/max-peers" : 1,
                     "onyx/min-peers" : 1,
                     "color/channel" : "brightness",
                     "onyx/doc" : "Logs the color to the console for debugging",
                     "onyx/batch-size" : 10
                     }
       }, {
           "task-map" : {
                         "onyx/min-peers" : 1,
                                          "onyx/max-peers" : 1,
                         "onyx/batch-size" : 10,
                         "onyx/batch-timeout" : 1000,
                         "onyx/name" : "4a39db00-55c4-4f4f-937f-255b64af07ee/inner",
                         "onyx/fn" : "clojure.core/identity",
                         "onyx/type" : "function"
                         }
           }, {
               "task-map" : {
                             "onyx/min-peers" : 1,
                                              "onyx/max-peers" : 1,
                             "onyx/batch-size" : 10,
                             "onyx/batch-timeout" : 1000,
                             "onyx/name" : "4a39db00-55c4-4f4f-937f-255b64af07ee/out",
                             "onyx/fn" : "clojure.core/identity",
                             "onyx/type" : "function"
                             }
               }, {
                   "task-map" : {
                                 "onyx/min-peers" : 1,
                                                  "onyx/max-peers" : 1,
                                 "onyx/batch-size" : 10,
                                 "onyx/batch-timeout" : 1000,
                                 "onyx/name" : "f106526c-d92a-4535-9b02-80dabf7d8a9e/in",
                                 "onyx/plugin" : "onyx.plugin.core-async/output",
                                 "onyx/type" : "output",
                                 "onyx/medium" : "core.async",
                                 "onyx/doc" : "Publishes segments to core.async"
                                 },
                              "lifecycles" : [ {
                                                "lifecycle/task" : "f106526c-d92a-4535-9b02-80dabf7d8a9e/in",
                                                                 "lifecycle/calls" : "guadalete.onyx.tasks.async/publish-calls"
                                                }, {
                                                    "lifecycle/task" : "f106526c-d92a-4535-9b02-80dabf7d8a9e/in",
                                                                     "lifecycle/calls" : "onyx.plugin.core-async/writer-calls"
                                                    } ]
                   } ]