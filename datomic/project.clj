(defproject com.intception.components/datomic "0.1.13"
  :description "Datomic Component"
  :url "https://github.com/guilespi/components"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.intception.components/lifecycle "0.1.7"]
                 [com.datomic/datomic-pro "0.9.5786" :exclusions [org.slf4j/slf4j-nop org.slf4j/log4j-over-slf4j com.google.guava/guava]]])
