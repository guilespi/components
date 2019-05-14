(ns components.jetty.service
  (:require [ring.adapter.jetty :as jetty])
  (:use components.lifecycle.protocol))

(defrecord JettyWeb [state config]
  Lifecycle
  (stop [this system]
    (when (:jetty @state)
      (.stop (:jetty @state))))
  (start [this system]
    (let [{:keys [routes port request-header-size
                  use-ssl ssl-port ssl-keystore
                  ssl-keystore-password]} config]
      (stop this system)
      (swap! state
             assoc :jetty
             (jetty/run-jetty (if (fn? routes)
                                (routes system)
                                routes)
                              (merge {:join? false
                                      :port port
                                      :send-server-version? false}
                                     (when request-header-size
                                       {:request-header-size request-header-size})
                                     (when use-ssl
                                       {:ssl? true
                                        :ssl-port (or ssl-port 443)
                                        :keystore ssl-keystore
                                        :key-password ssl-keystore-password}))))))

  Service
  (handler [_]
    (:jetty @state)))

(defn make
  "Creates a jetty web server component"
 [config]
 (->JettyWeb (atom {}) config))
