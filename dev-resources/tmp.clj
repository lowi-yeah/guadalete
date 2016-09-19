{
 :light-attributez
 {:id "lght-an-mqtt-light", :ilk :light, :item-id "an-mqtt-light", :position {:x 614, :y 70}, :links [{:id "in", :accepts :color, :direction :in, :index 0}]}
 :item
 {:transport :mqtt, :color {:brightness 0, :saturation 0, :hue 0}, :room-id "w00t", :name "an-mqtt-light", :type :hsv, :accepted? true, :id "an-mqtt-light"}
 :attributez
 {:transport :mqtt, :color-type :hsv, :client-id "lght-an-mqtt-light", :task :guadalete.onyx.tasks.items.light/in, :name :an-mqtt-light-in, :type :light/in, :topic "he110", :broker "tcp://mosquitto1:1883", :color-fn :guadalete.onyx.tasks.items.light/hsv->rgb, :mqtt-id "an-mqtt-light"}}
