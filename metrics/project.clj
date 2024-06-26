(defproject com.intception.components/metrics "0.1.12"
  :description "metrics component"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [metrics-clojure "2.7.0"]
                 [metrics-clojure-jvm "2.7.0"]
                 [metrics-clojure-ring "2.7.0"]
                 [metrics-clojure-graphite "2.7.0"]
                 [org.clojars.intception/thread-expr "1.4.0"]
                 [com.intception.components/lifecycle "0.1.6"]])
