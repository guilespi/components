(ns components.app.service
  (:use components.lifecycle.protocol))

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

(defn ->by-class-name
  [app component-class-name]
  (filter #(re-find (re-pattern component-class-name)
                    ((comp (memfn getName) class) %))
          (:components app)))

(defn ->by-component-id
  [app component-id]
  (filter #(and (satisfies? Identifier %)
                (= component-id
                   (get-id %)))
          (:components app)))


(defn ->components
  [app service]
  (->by-class-name app (format "^components\\.%s\\.service\\.[^.]+$"
                               (name service))))

(defn ->handler
  [app service]
  (when-let [s (first (->components app service))]
    (handler s)))
