(defproject com.intception.components/time-series "0.1.15"
  :description "Time Series Component"
  :url "https://github.com/guilespi/components"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.intception.components/lifecycle "0.1.6"]
                 [com.jolbox/bonecp "0.8.0.RELEASE"]
                 [org.slf4j/slf4j-log4j12 "1.7.7"]
                 [time-series-storage "0.3.3"]])
