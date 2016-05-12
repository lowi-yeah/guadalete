(ns guadalete.utils.mock
    (:require [taoensso.timbre :as log]))

(def weather {:latitude  48.2228675
              :longitude 16.3817916
              :timezone  "Europe/Vienna"
              :offset    2
              :currently {:pressure            998.18
                          :ozone               349.3
                          :cloudCover          0.46
                          :precipIntensity     0.3505
                          :windSpeed           4.29
                          :apparentTemperature 15.64
                          :time                1463067663
                          :precipType          "rain"
                          :icon                "rain"
                          :humidity            0.9
                          :summary             "Drizzle"
                          :dewPoint            14.01
                          :precipProbability   0.54
                          :windBearing         92
                          :temperature         15.64}
              :daily     {:summary "Light rain throughout the week, with temperatures peaking at 20°C on Saturday."
                          :icon    "rain"
                          :data    [{:precipIntensityMaxTime     1463086800
                                     :pressure                   1001.46
                                     :apparentTemperatureMaxTime 1463058000
                                     :ozone                      352.34
                                     :cloudCover                 0.5
                                     :temperatureMax             16.23
                                     :precipIntensity            0.4115
                                     :windSpeed                  3.97
                                     :apparentTemperatureMax     16.23
                                     :time                       1463004000
                                     :precipType                 "rain"
                                     :precipIntensityMax         1.5672
                                     :icon                       "rain"
                                     :temperatureMin             10.34
                                     :moonPhase                  0.21
                                     :humidity                   0.94
                                     :summary                    "Rain throughout the day."
                                     :apparentTemperatureMin     10.34
                                     :apparentTemperatureMinTime 1463014800
                                     :dewPoint                   12.25
                                     :precipProbability          0.72
                                     :temperatureMinTime         1463014800
                                     :temperatureMaxTime         1463058000
                                     :windBearing                106
                                     :sunsetTime                 1463077465
                                     :sunriseTime                1463023195}
                                    {:precipIntensityMaxTime     1463097600
                                     :pressure                   995.57
                                     :apparentTemperatureMaxTime 1463148000
                                     :ozone                      363.08
                                     :cloudCover                 0.89
                                     :temperatureMax             18.66
                                     :precipIntensity            0.6985
                                     :windSpeed                  1.54
                                     :apparentTemperatureMax     18.66
                                     :time                       1463090400
                                     :precipType                 "rain"
                                     :precipIntensityMax         1.9736
                                     :icon                       "rain"
                                     :temperatureMin             11.23
                                     :moonPhase                  0.24
                                     :humidity                   0.87
                                     :summary                    "Rain throughout the day."
                                     :apparentTemperatureMin     11.23
                                     :apparentTemperatureMinTime 1463108400
                                     :dewPoint                   11.92
                                     :precipProbability          0.74
                                     :temperatureMinTime         1463108400
                                     :temperatureMaxTime         1463148000
                                     :windBearing                273
                                     :sunsetTime                 1463163946
                                     :sunriseTime                1463109515}
                                    {:precipIntensityMaxTime     1463259600
                                     :pressure                   1001.38
                                     :apparentTemperatureMaxTime 1463227200
                                     :ozone                      363.21
                                     :cloudCover                 0.92
                                     :temperatureMax             20.21
                                     :precipIntensity            0.4699
                                     :windSpeed                  4.44
                                     :apparentTemperatureMax     20.21
                                     :time                       1463176800
                                     :precipType                 "rain"
                                     :precipIntensityMax         1.9025
                                     :icon                       "rain"
                                     :temperatureMin             9.91
                                     :moonPhase                  0.27
                                     :humidity                   0.85
                                     :summary                    "Rain in the morning and afternoon."
                                     :apparentTemperatureMin     6.88
                                     :apparentTemperatureMinTime 1463259600
                                     :dewPoint                   11.23
                                     :precipProbability          0.74
                                     :temperatureMinTime         1463259600
                                     :temperatureMaxTime         1463227200
                                     :windBearing                310
                                     :sunsetTime                 1463250426
                                     :sunriseTime                1463195836}
                                    {:precipIntensityMaxTime     1463266800
                                     :pressure                   1012.93
                                     :apparentTemperatureMaxTime 1463317200
                                     :ozone                      385.3
                                     :cloudCover                 0.69
                                     :temperatureMax             15.43
                                     :precipIntensity            0.3759
                                     :windSpeed                  7.59
                                     :apparentTemperatureMax     15.43
                                     :time                       1463263200
                                     :precipType                 "rain"
                                     :precipIntensityMax         2.1006
                                     :icon                       "wind"
                                     :temperatureMin             6.22
                                     :moonPhase                  0.3
                                     :humidity                   0.73
                                     :summary                    "Breezy and mostly cloudy until afternoon."
                                     :apparentTemperatureMin     1.92
                                     :apparentTemperatureMinTime 1463274000
                                     :dewPoint                   4.59
                                     :precipProbability          0.75
                                     :temperatureMinTime         1463274000
                                     :temperatureMaxTime         1463317200
                                     :windBearing                315
                                     :sunsetTime                 1463336906
                                     :sunriseTime                1463282159}
                                    {:precipIntensityMaxTime     1463410800
                                     :pressure                   1018.68
                                     :apparentTemperatureMaxTime 1463403600
                                     :ozone                      408.78
                                     :cloudCover                 0.02
                                     :temperatureMax             14.33
                                     :precipIntensity            0.0279
                                     :windSpeed                  6.59
                                     :apparentTemperatureMax     14.33
                                     :time                       1463349600
                                     :precipType                 "rain"
                                     :precipIntensityMax         0.066
                                     :icon                       "clear-day"
                                     :temperatureMin             5.26
                                     :moonPhase                  0.33
                                     :humidity                   0.69
                                     :summary                    "Clear throughout the day."
                                     :apparentTemperatureMin     1.17
                                     :apparentTemperatureMinTime 1463367600
                                     :dewPoint                   3.34
                                     :precipProbability          0.08
                                     :temperatureMinTime         1463367600
                                     :temperatureMaxTime         1463403600
                                     :windBearing                303
                                     :sunsetTime                 1463423384
                                     :sunriseTime                1463368484}
                                    {:precipIntensityMaxTime     1463497200
                                     :pressure                   1017.96
                                     :apparentTemperatureMaxTime 1463493600
                                     :ozone                      409.39
                                     :cloudCover                 0.14
                                     :temperatureMax             14.04
                                     :precipIntensity            0.0356
                                     :windSpeed                  6.12
                                     :apparentTemperatureMax     14.04
                                     :time                       1463436000
                                     :precipType                 "rain"
                                     :precipIntensityMax         0.0762
                                     :icon                       "partly-cloudy-night"
                                     :temperatureMin             4.12
                                     :moonPhase                  0.36
                                     :humidity                   0.72
                                     :summary                    "Partly cloudy starting in the evening."
                                     :apparentTemperatureMin     0.08
                                     :apparentTemperatureMinTime 1463450400
                                     :dewPoint                   3.87
                                     :precipProbability          0.1
                                     :temperatureMinTime         1463450400
                                     :temperatureMaxTime         1463493600
                                     :windBearing                295
                                     :sunsetTime                 1463509862
                                     :sunriseTime                1463454811}
                                    {:precipIntensityMaxTime     1463562000
                                     :pressure                   1014.89
                                     :apparentTemperatureMaxTime 1463580000
                                     :ozone                      388.99
                                     :cloudCover                 0.6
                                     :temperatureMax             15.84
                                     :precipIntensity            0.2489
                                     :windSpeed                  3.08
                                     :apparentTemperatureMax     15.84
                                     :time                       1463522400
                                     :precipType                 "rain"
                                     :precipIntensityMax         1.0871
                                     :icon                       "rain"
                                     :temperatureMin             3.87
                                     :moonPhase                  0.39
                                     :humidity                   0.79
                                     :summary                    "Light rain until afternoon."
                                     :apparentTemperatureMin     0.8
                                     :apparentTemperatureMinTime 1463536800
                                     :dewPoint                   5.61
                                     :precipProbability          0.68
                                     :temperatureMinTime         1463536800
                                     :temperatureMaxTime         1463580000
                                     :windBearing                278
                                     :sunsetTime                 1463596339
                                     :sunriseTime                1463541140}
                                    {:precipIntensityMaxTime     1463630400
                                     :pressure                   1013.63
                                     :apparentTemperatureMaxTime 1463662800
                                     :ozone                      383.5
                                     :cloudCover                 0.58
                                     :temperatureMax             18.61
                                     :precipIntensity            0.1499
                                     :windSpeed                  2.28
                                     :apparentTemperatureMax     18.61
                                     :time                       1463608800
                                     :precipType                 "rain"
                                     :precipIntensityMax         0.2769
                                     :icon                       "rain"
                                     :temperatureMin             5.12
                                     :moonPhase                  0.42
                                     :humidity                   0.76
                                     :summary                    "Light rain in the morning and afternoon."
                                     :apparentTemperatureMin     4.9
                                     :apparentTemperatureMinTime 1463612400
                                     :dewPoint                   7.84
                                     :precipProbability          0.51
                                     :temperatureMinTime         1463616000
                                     :temperatureMaxTime         1463662800
                                     :windBearing                302
                                     :sunsetTime                 1463682815
                                     :sunriseTime                1463627471}]}})