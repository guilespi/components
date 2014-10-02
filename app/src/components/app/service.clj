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

(defn ->components
  [app service]
  (filter #(re-find (re-pattern (format "^components\\.%s\\.service\\.[^.]+$"
                                        (name service)))
                    ((comp (memfn getName) class) %))
          (:components app)))

(defn ->handler
  [app service]
  (when-let [s (first (get-component app service))]
    (handler s)))
