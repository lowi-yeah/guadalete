(ns guadalete.config.graph
    (:require
      [taoensso.timbre :as log]
      [schema.core :as s]
      [onyx.schema :as os]))

(s/defn ^:always-validate task*
        [ns* :- s/Keyword
         fn* :- s/Keyword]
        (keyword (str "guadalete.onyx.tasks.items." (name ns*)) (name fn*)))

(s/defn attributes-for-type
        "Look up the attributes reqired to perform the task for a given ubergraph-node-type"
        [type node item]
        (condp = type
               :signal/out {:type      type
                            :name      (keyword (:id item) (name type))
                            :task      (task* :signal :in)
                            :signal-id (:id item)
                            ;:id        (:id node)
                            }

               :mixer/in-0 {:type type
                            :name (keyword (:id item) (name type))
                            :task (task* :mixer :in)}

               :mixer/in-1 {:type type
                            :name (keyword (:id item) (name type))
                            :task (task* :mixer :in)}

               :mixer/inner {:type     type
                             :name     (keyword (:id item) (name type))
                             :task     (task* :mixer :mix!)
                             :mixin-fn (keyword "guadalete.onyx.items.mixer" (name (:mixin-fn item)))}

               :mixer/out {:type type
                           :name (keyword (:id item) (name type))
                           :task (task* :mixer :out)}

               :color/brightness {:type    type
                                  :channel (keyword (name type))
                                  :name    (keyword (:id item) (name type))
                                  :task    (task* :color :in)}

               :color/saturation {:type    type
                                  :channel (keyword (name type))
                                  :name    (keyword (:id item) (name type))
                                  :task    (task* :color :in)}

               :color/hue {:type    type
                           :channel (keyword (name type))
                           :name    (keyword (:id item) (name type))
                           :task    (task* :color :in)}

               :color/inner {:type type
                             :name (keyword (:id item) (name type))
                             :task (task* :color :inner)}

               :color/out {:type type
                           :name (keyword (:id item) (name type))
                           :task (task* :color :out)}

               :light/in {:type type
                          :name (keyword (:id item) (name type))
                          :task (task* :light :in)}))
