(ns components.jetty.service
  (require [ring.adapter.jetty :as jetty])
  (use components.lifecycle.protocol))

(defrecord JettyWeb [state routes port]
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
                            {:join? false :port port})))

  Service
  (handler [_]
    (:jetty @state)))

(defn make
  "Creates a jetty web server component"
 [{:keys [routes port]}]
 (->JettyWeb (atom {}) routes port))
