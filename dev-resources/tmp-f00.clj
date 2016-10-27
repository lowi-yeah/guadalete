{:id "foo"
 :name "foo"
 :room-id "f00"
 :mode :none
 :translation {:x 0
 :y 0}
 :nodes {:clr-2270c0c5-5730-4962-b164-d63e5d2e0bf4 {:id "clr-2270c0c5-5730-4962-b164-d63e5d2e0bf4"
 :ilk :color
 :item-id "2270c0c5-5730-4962-b164-d63e5d2e0bf4"
 :position {:x 390
 :y 101}
 :links [{:index 0
 :name "brightness"
 :id "brightness"
 :accepts :value
 :direction :in}
{:id "out"
 :emits :color
 :direction :out
 :index 1
 :name "out"}]}
 :kon-3bb93c67-f463-4f0b-9847-ee543d822c8e {:id "kon-3bb93c67-f463-4f0b-9847-ee543d822c8e"
 :ilk :constant
 :item-id "20efe589-2785-476f-85db-0e0541237bf8"
 :position {:x 11
 :y 94}
 :links [{:id "out"
 :emits :value
 :direction :out
 :index 0}]}
 :lght-f00 {:id "lght-f00"
 :ilk :light
 :item-id "f00"
 :position {:x 535
 :y 122}
 :links [{:id "in"
 :accepts :color
 :direction :in
 :index 0}]}
 :mix-88d064ec-c8cc-4d4e-a137-78db4a8e89d9 {:id "mix-88d064ec-c8cc-4d4e-a137-78db4a8e89d9"
 :ilk :mixer
 :item-id "88d064ec-c8cc-4d4e-a137-78db4a8e89d9"
 :position {:x 223
 :y 80}
 :links [{:index 0
 :name "first"
 :id "in-0"
 :accepts :value
 :direction :in}
{:index 1
 :name "second"
 :id "in-1"
 :accepts :value
 :direction :in}
{:id "out"
 :emits :value
 :direction :out
 :index 2
 :name "out"}]}
 :sgnl-78635f5e-0cb9-48ec-822b-5efd244457f4 {:id "sgnl-78635f5e-0cb9-48ec-822b-5efd244457f4"
 :ilk :signal
 :item-id "quicksine"
 :position {:x 10
 :y 146}
 :links [{:id "out"
 :emits :value
 :direction :out
 :index 0}]}}
 :flows {:35cb924b-2d41-402c-86d9-e178b9df1b5a {:from {:id "out"
 :node-id "clr-2270c0c5-5730-4962-b164-d63e5d2e0bf4"
 :scene-id "foo"}
 :id "35cb924b-2d41-402c-86d9-e178b9df1b5a"
 :to {:id "in"
 :node-id "lght-f00"
 :scene-id "foo"}}
 :51103734-aaab-4475-a768-fe06f9bf140f {:from {:id "out"
 :node-id "mix-88d064ec-c8cc-4d4e-a137-78db4a8e89d9"
 :scene-id "foo"}
 :id "51103734-aaab-4475-a768-fe06f9bf140f"
 :to {:id "brightness"
 :node-id "clr-2270c0c5-5730-4962-b164-d63e5d2e0bf4"
 :scene-id "foo"}}
 :8092f2b9-7642-49a7-82d8-4c1d94f1290e {:from {:id "out"
 :node-id "sgnl-78635f5e-0cb9-48ec-822b-5efd244457f4"
 :scene-id "foo"}
 :id "8092f2b9-7642-49a7-82d8-4c1d94f1290e"
 :to {:id "in-1"
 :node-id "mix-88d064ec-c8cc-4d4e-a137-78db4a8e89d9"
 :scene-id "foo"}}
 :acf38355-e351-48cd-a3f0-b1050e2525d9 {:from {:id "out"
 :node-id "kon-3bb93c67-f463-4f0b-9847-ee543d822c8e"
 :scene-id "foo"}
 :id "acf38355-e351-48cd-a3f0-b1050e2525d9"
 :to {:id "in-0"
 :node-id "mix-88d064ec-c8cc-4d4e-a137-78db4a8e89d9"
 :scene-id "foo"}}}
 :on? false}