(ns components.app.service
  (use components.lifecycle.protocol))

(defrecord App [components]
  Lifecycle
  (stop [this]
    (doseq [c components]
      (stop c this)))
  (start [this]
    (doseq [c components]
      (start c this))))

(defn make
  "Creates an app component component"
 [& components]
 (->App components))
