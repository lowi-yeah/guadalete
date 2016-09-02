{:onyx.core/batch ({:message {:data 0.28056129405106717
                                              :id "sine"
                                              :at 1472548380720}
                                    :id "c3526ff4-94a3-299b-9097-a133fe78efd7"
                                    :offset 54851
                                    :acker-id "b87a0292-fc42-494c-b598-f9ec7c986cb9"
                                    :completion-id "46b4cbd3-14f5-47c2-b93b-54db63e95173"
                                    :ack-val nil
                                    :hash-group nil
                                    :route nil})

 :onyx.core/workflow [[:sine/in :sine/out]
                      [:sine/out :f286e22d-19ca-4c12-a287-6b6315c3523a]
                      [:slowsine/in :slowsine/out]
                      [:slowsine/out :f286e22d-19ca-4c12-a287-6b6315c3523a]
                      [:f286e22d-19ca-4c12-a287-6b6315c3523a :e8bbf866-e2db-4465-8390-a6378b46fa29]
                      [:e8bbf866-e2db-4465-8390-a6378b46fa29 :1fc2b83a-fe56-4b3c-869e-e4350e086257]]

 :onyx.core/task-map {:kafka/zookeeper "zookeeper1:2181"
                      :onyx/plugin :onyx.plugin.kafka/read-messages
                      :onyx/medium :kafka
                      :kafka/offset-reset :largest
                      :kafka/force-reset? true
                      :onyx/batch-timeout 1000
                      :onyx/type :input
                      :onyx/name :sine/in
                      :kafka/topic "gdlt-sgnl-v"
                      :kafka/group-id "sine-in"
                      :onyx/max-peers 1
                      :onyx/min-peers 1
                      :onyx/doc "Consumes gdlt-sgnl-v messages from Kafka"
                      :kafka/empty-read-back-off 50
                      :kafka/fetch-size 307200
                      :onyx/batch-size 10
                      :kafka/deserializer-fn :guadalete.onyx.tasks.util/deserialize-message-json
                      :kafka/wrap-with-metadata? false
                      :kafka/commit-interval 50
                      :kafka/chan-capacity 100}
 :onyx.core/flow-conditions [{:flow/from :sine/in
                              :flow/to [:sine/out]
                              :flow/predicate :guadalete.onyx.filters/signal-id}
                             {:flow/from :slowsine/in
                              :flow/to [:slowsine/out]
                              :flow/predicate :guadalete.onyx.filters/signal-id}]

 :onyx.core/task-information #onyx.peer.task_lifecycle.TaskInformation{:id #uuid "46b4cbd3-14f5-47c2-b93b-54db63e95173"
                                                                       :log #<ZooKeeper Component>
                                                                       :job-id #uuid "0ef9dd00-cc97-4e9f-8e1b-3aa329f6d7a6"
                                                                       :task-id :sine/in
                                                                       :workflow [[:sine/in :sine/out]
                                                                                  [:sine/out :f286e22d-19ca-4c12-a287-6b6315c3523a]
                                                                                  [:slowsine/in :slowsine/out]
                                                                                  [:slowsine/out :f286e22d-19ca-4c12-a287-6b6315c3523a]
                                                                                  [:f286e22d-19ca-4c12-a287-6b6315c3523a :e8bbf866-e2db-4465-8390-a6378b46fa29]
                                                                                  [:e8bbf866-e2db-4465-8390-a6378b46fa29 :1fc2b83a-fe56-4b3c-869e-e4350e086257]]
                                                                       :catalog [{:kafka/zookeeper "zookeeper1:2181"
                                                                                  :onyx/plugin :onyx.plugin.kafka/read-messages
                                                                                  :onyx/medium :kafka
                                                                                  :kafka/offset-reset :largest
                                                                                  :kafka/force-reset? true
                                                                                  :onyx/batch-timeout 1000
                                                                                  :onyx/type :input
                                                                                  :onyx/name :slowsine/in
                                                                                  :kafka/topic "gdlt-sgnl-v"
                                                                                  :kafka/group-id "slowsine-in"
                                                                                  :onyx/max-peers 1
                                                                                  :onyx/min-peers 1
                                                                                  :onyx/doc "Consumes gdlt-sgnl-v messages from Kafka"
                                                                                  :kafka/empty-read-back-off 50
                                                                                  :kafka/fetch-size 307200
                                                                                  :onyx/batch-size 10
                                                                                  :kafka/deserializer-fn :guadalete.onyx.tasks.util/deserialize-message-json
                                                                                  :kafka/wrap-with-metadata? false
                                                                                  :kafka/commit-interval 50
                                                                                  :kafka/chan-capacity 100}
                                                                                 {:onyx/min-peers 1
                                                                                  :onyx/max-peers 1
                                                                                  :onyx/batch-size 10
                                                                                  :onyx/batch-timeout 1000
                                                                                  :onyx/name :slowsine/out
                                                                                  :onyx/fn :guadalete.onyx.tasks.scene/log-segment
                                                                                  :onyx/type :function
                                                                                  :onyx/doc "The identity function with a log"}
                                                                                 {:kafka/zookeeper "zookeeper1:2181"
                                                                                  :onyx/plugin :onyx.plugin.kafka/read-messages
                                                                                  :onyx/medium :kafka
                                                                                  :kafka/offset-reset :largest
                                                                                  :kafka/force-reset? true
                                                                                  :onyx/batch-timeout 1000
                                                                                  :onyx/type :input
                                                                                  :onyx/name :sine/in
                                                                                  :kafka/topic "gdlt-sgnl-v"
                                                                                  :kafka/group-id "sine-in"
                                                                                  :onyx/max-peers 1
                                                                                  :onyx/min-peers 1
                                                                                  :onyx/doc "Consumes gdlt-sgnl-v messages from Kafka"
                                                                                  :kafka/empty-read-back-off 50
                                                                                  :kafka/fetch-size 307200
                                                                                  :onyx/batch-size 10
                                                                                  :kafka/deserializer-fn :guadalete.onyx.tasks.util/deserialize-message-json
                                                                                  :kafka/wrap-with-metadata? false
                                                                                  :kafka/commit-interval 50
                                                                                  :kafka/chan-capacity 100}
                                                                                 {:onyx/min-peers 1
                                                                                  :onyx/max-peers 1
                                                                                  :onyx/batch-size 10
                                                                                  :onyx/batch-timeout 1000
                                                                                  :onyx/name :sine/out
                                                                                  :onyx/fn :guadalete.onyx.tasks.scene/log-segment
                                                                                  :onyx/type :function
                                                                                  :onyx/doc "The identity function with a log"}
                                                                                 {:onyx/min-peers 1
                                                                                  :onyx/max-peers 1
                                                                                  :onyx/batch-size 10
                                                                                  :onyx/batch-timeout 1000
                                                                                  :onyx/name :f286e22d-19ca-4c12-a287-6b6315c3523a
                                                                                  :onyx/fn :guadalete.onyx.tasks.mixer/log-mixer
                                                                                  :onyx/type :function
                                                                                  :onyx/uniqueness-key :at
                                                                                  :onyx/doc "Logs the color to the console for debugging"}
                                                                                 {:onyx/min-peers 1
                                                                                  :onyx/max-peers 1
                                                                                  :onyx/batch-size 10
                                                                                  :onyx/batch-timeout 1000
                                                                                  :onyx/name :e8bbf866-e2db-4465-8390-a6378b46fa29
                                                                                  :onyx/fn :guadalete.onyx.tasks.scene/log-color
                                                                                  :onyx/type :function
                                                                                  :onyx/uniqueness-key :at
                                                                                  :onyx/doc "Logs the color to the console for debugging"}
                                                                                 {:onyx/name :1fc2b83a-fe56-4b3c-869e-e4350e086257
                                                                                  :onyx/plugin :onyx.plugin.core-async/output
                                                                                  :onyx/type :output
                                                                                  :onyx/medium :core.async
                                                                                  :onyx/doc "Writes segments to a core.async channel"
                                                                                  :onyx/min-peers 1
                                                                                  :onyx/max-peers 1
                                                                                  :onyx/batch-size 10
                                                                                  :onyx/batch-timeout 1000}]
                                                                       :task {:id :sine/in
                                                                              :name :sine/in
                                                                              :egress-ids {:sine/out :sine/out}}
                                                                       :flow-conditions [{:flow/from :sine/in
                                                                                          :flow/to [:sine/out]
                                                                                          :flow/predicate :guadalete.onyx.filters/signal-id}
                                                                                         {:flow/from :slowsine/in
                                                                                          :flow/to [:slowsine/out]
                                                                                          :flow/predicate :guadalete.onyx.filters/signal-id}]
                                                                       :windows []
                                                                       :filtered-windows []
                                                                       :triggers []
                                                                       :lifecycles [{:lifecycle/task :slowsine/in
                                                                                     :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}
                                                                                    {:lifecycle/task :sine/in
                                                                                     :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}
                                                                                    {:lifecycle/task :1fc2b83a-fe56-4b3c-869e-e4350e086257
                                                                                     :lifecycle/calls :guadalete.onyx.tasks.async/out-calls
                                                                                     :id :1fc2b83a-fe56-4b3c-869e-e4350e086257}
                                                                                    {:lifecycle/task :1fc2b83a-fe56-4b3c-869e-e4350e086257
                                                                                     :lifecycle/calls :onyx.plugin.core-async/writer-calls}]
                                                                       :task-map {:kafka/zookeeper "zookeeper1:2181"
                                                                                  :onyx/plugin :onyx.plugin.kafka/read-messages
                                                                                  :onyx/medium :kafka
                                                                                  :kafka/offset-reset :largest
                                                                                  :kafka/force-reset? true
                                                                                  :onyx/batch-timeout 1000
                                                                                  :onyx/type :input
                                                                                  :onyx/name :sine/in
                                                                                  :kafka/topic "gdlt-sgnl-v"
                                                                                  :kafka/group-id "sine-in"
                                                                                  :onyx/max-peers 1
                                                                                  :onyx/min-peers 1
                                                                                  :onyx/doc "Consumes gdlt-sgnl-v messages from Kafka"
                                                                                  :kafka/empty-read-back-off 50
                                                                                  :kafka/fetch-size 307200
                                                                                  :onyx/batch-size 10
                                                                                  :kafka/deserializer-fn :guadalete.onyx.tasks.util/deserialize-message-json
                                                                                  :kafka/wrap-with-metadata? false
                                                                                  :kafka/commit-interval 50
                                                                                  :kafka/chan-capacity 100}
                                                                       :task-name :sine/in
                                                                       :filtered-triggers []
                                                                       :metadata {:job-id #uuid "0ef9dd00-cc97-4e9f-8e1b-3aa329f6d7a6"
                                                                                  :job-hash "95c32b94b89a927bca82b13ca9da62022227f3a616689d9ce2a57f8b2255320"}}
 :onyx.core/catalog [{:kafka/zookeeper "zookeeper1:2181"
                      :onyx/plugin :onyx.plugin.kafka/read-messages
                      :onyx/medium :kafka
                      :kafka/offset-reset :largest
                      :kafka/force-reset? true
                      :onyx/batch-timeout 1000
                      :onyx/type :input
                      :onyx/name :slowsine/in
                      :kafka/topic "gdlt-sgnl-v"
                      :kafka/group-id "slowsine-in"
                      :onyx/max-peers 1
                      :onyx/min-peers 1
                      :onyx/doc "Consumes gdlt-sgnl-v messages from Kafka"
                      :kafka/empty-read-back-off 50
                      :kafka/fetch-size 307200
                      :onyx/batch-size 10
                      :kafka/deserializer-fn :guadalete.onyx.tasks.util/deserialize-message-json
                      :kafka/wrap-with-metadata? false
                      :kafka/commit-interval 50
                      :kafka/chan-capacity 100}
                     {:onyx/min-peers 1
                      :onyx/max-peers 1
                      :onyx/batch-size 10
                      :onyx/batch-timeout 1000
                      :onyx/name :slowsine/out
                      :onyx/fn :guadalete.onyx.tasks.scene/log-segment
                      :onyx/type :function
                      :onyx/doc "The identity function with a log"}
                     {:kafka/zookeeper "zookeeper1:2181"
                      :onyx/plugin :onyx.plugin.kafka/read-messages
                      :onyx/medium :kafka
                      :kafka/offset-reset :largest
                      :kafka/force-reset? true
                      :onyx/batch-timeout 1000
                      :onyx/type :input
                      :onyx/name :sine/in
                      :kafka/topic "gdlt-sgnl-v"
                      :kafka/group-id "sine-in"
                      :onyx/max-peers 1
                      :onyx/min-peers 1
                      :onyx/doc "Consumes gdlt-sgnl-v messages from Kafka"
                      :kafka/empty-read-back-off 50
                      :kafka/fetch-size 307200
                      :onyx/batch-size 10
                      :kafka/deserializer-fn :guadalete.onyx.tasks.util/deserialize-message-json
                      :kafka/wrap-with-metadata? false
                      :kafka/commit-interval 50
                      :kafka/chan-capacity 100}
                     {:onyx/min-peers 1
                      :onyx/max-peers 1
                      :onyx/batch-size 10
                      :onyx/batch-timeout 1000
                      :onyx/name :sine/out
                      :onyx/fn :guadalete.onyx.tasks.scene/log-segment
                      :onyx/type :function
                      :onyx/doc "The identity function with a log"}
                     {:onyx/min-peers 1
                      :onyx/max-peers 1
                      :onyx/batch-size 10
                      :onyx/batch-timeout 1000
                      :onyx/name :f286e22d-19ca-4c12-a287-6b6315c3523a
                      :onyx/fn :guadalete.onyx.tasks.mixer/log-mixer
                      :onyx/type :function
                      :onyx/uniqueness-key :at
                      :onyx/doc "Logs the color to the console for debugging"}
                     {:onyx/min-peers 1
                      :onyx/max-peers 1
                      :onyx/batch-size 10
                      :onyx/batch-timeout 1000
                      :onyx/name :e8bbf866-e2db-4465-8390-a6378b46fa29
                      :onyx/fn :guadalete.onyx.tasks.scene/log-color
                      :onyx/type :function
                      :onyx/uniqueness-key :at
                      :onyx/doc "Logs the color to the console for debugging"}
                     {:onyx/name :1fc2b83a-fe56-4b3c-869e-e4350e086257
                      :onyx/plugin :onyx.plugin.core-async/output
                      :onyx/type :output
                      :onyx/medium :core.async
                      :onyx/doc "Writes segments to a core.async channel"
                      :onyx/min-peers 1
                      :onyx/max-peers 1
                      :onyx/batch-size 10
                      :onyx/batch-timeout 1000}]
                                                                                                                                                                                                :id "sine"
                                                                                                                                                                                                :at 1472548380720}
                                                                                                                                                                                      :id #uuid "c3526ff4-94a3-299b-9097-a133fe78efd7"
                                                                                                                                                                                      :offset 54851
                                                                                                                                                                                      :acker-id nil
                                                                                                                                                                                      :completion-id nil
                                                                                                                                                                                      :ack-val nil
                                                                                                                                                                                      :hash-group nil
                                                                                                                                                                                      :route nil}}}]
 :onyx.core/task-id :sine/in
 :onyx.core/lifecycles [{:lifecycle/task :slowsine/in
                         :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}
                        {:lifecycle/task :sine/in
                         :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}
                        {:lifecycle/task :1fc2b83a-fe56-4b3c-869e-e4350e086257
                         :lifecycle/calls :guadalete.onyx.tasks.async/out-calls
                         :id :1fc2b83a-fe56-4b3c-869e-e4350e086257}
                        {:lifecycle/task :1fc2b83a-fe56-4b3c-869e-e4350e086257
                         :lifecycle/calls :onyx.plugin.core-async/writer-calls}]
 }