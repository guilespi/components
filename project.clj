(defproject intception.com/components "0.1.0"
  :description "Reusable services implementation following the components pattern"
  :url "https://github.com/guilespi/components"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [intception.com.components/lifecycle "0.1.0"]
                 [intception.com.components/jetty "0.1.0"]
                 [intception.com.components/datomic "0.1.0"]]
  :plugins [[lein-sub "0.2.4"]
            [codox "0.8.5"]]
  :sub ["lifecycle"
        "jetty"
        "datomic"])
