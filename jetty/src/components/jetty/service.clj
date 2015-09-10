(ns components.jetty.service
  (require [ring.adapter.jetty :as jetty])
  (use components.lifecycle.protocol))

(defrecord JettyWeb [state routes port request-header-size]
  Lifecycle
  (stop [this system]
    (when (:jetty @state)
      (.stop (:jetty @state))))
  (start [this system]
    (stop this system)
    (swap! state
           assoc :jetty
           (jetty/run-jetty (if (fn? routes)
                              (routes system)
                              routes)
                            {:join? false
                             :port port
                             :configurator (fn [jetty]
                                             (when request-header-size
                                               (doseq [connector (.getConnectors jetty)]
                                                 (.setRequestHeaderSize connector request-header-size))))})))

  Service
  (handler [_]
    (:jetty @state)))

(defn make
  "Creates a jetty web server component"
 [{:keys [routes port request-header-size]}]
 (->JettyWeb (atom {}) routes port request-header-size))
