(

  {:name :foo
   :job  {:workflow       [[:sine :bf0ae803-faaa-4fe4-aa91-923bf38f172b]
                           [:bf0ae803-faaa-4fe4-aa91-923bf38f172b :28297753-25e0-45c3-be2a-794600de114a]]
          :lifecycles     [{:lifecycle/task  :read-messages
                            :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}
                           {:lifecycle/task  :28297753-25e0-45c3-be2a-794600de114a
                            :lifecycle/calls :guadalete.tasks.async/out-calls
                            :id              :28297753-25e0-45c3-be2a-794600de114a}
                           {:lifecycle/task  :28297753-25e0-45c3-be2a-794600de114a
                            :lifecycle/calls :onyx.plugin.core-async/writer-calls}]
          :catalog        [{:kafka/zookeeper           "zookeeper1:2181"
                            :onyx/plugin               :onyx.plugin.kafka/read-messages
                            :onyx/medium               :kafka
                            :kafka/offset-reset        :largest
                            :kafka/force-reset?        true
                            :onyx/batch-timeout        1000
                            :onyx/type                 :input
                            :onyx/name                 :read-messages
                            :kafka/topic               "gdlt-sgnl-v"
                            :kafka/group-id            "sine"
                            :onyx/max-peers            1
                            :onyx/min-peers            1
                            :onyx/doc                  "Reads messages from a Kafka topic"
                            :kafka/empty-read-back-off 50
                            :kafka/fetch-size          307200
                            :onyx/batch-size           1
                            :kafka/deserializer-fn     :guadalete.tasks.kafka/deserialize-message-json
                            :kafka/wrap-with-metadata? false
                            :kafka/commit-interval     50
                            :kafka/chan-capacity       100}
                           {:onyx/name           :bf0ae803-faaa-4fe4-aa91-923bf38f172b
                            :onyx/fn             :guadalete.tasks.items/color-log
                            :onyx/type           :function
                            :onyx/uniqueness-key :at
                            :onyx/doc            "Writes segments to a core.async channel"
                            :onyx/min-peers      1
                            :onyx/max-peers      1
                            :onyx/batch-size     10
                            :onyx/batch-timeout  1000}
                           {:onyx/name          :28297753-25e0-45c3-be2a-794600de114a
                            :onyx/plugin        :onyx.plugin.core-async/output
                            :onyx/type          :output
                            :onyx/medium        :core.async
                            :onyx/doc           "Writes segments to a core.async channel"
                            :onyx/min-peers     1
                            :onyx/max-peers     1
                            :onyx/batch-size    10
                            :onyx/batch-timeout 1000}]
          :triggers       []
          :windows        []
          :task-scheduler :onyx.task-scheduler/balanced}})