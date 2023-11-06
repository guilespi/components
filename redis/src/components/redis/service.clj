(ns components.redis.service
  (:require [taoensso.carmine :as car :refer (wcar)])
  (:use components.lifecycle.protocol))


(defrecord Redis [service-id state host port password]

  Lifecycle
  (stop [this system])
  (start [this system]
    (swap! state
           assoc :redis
           {:pool {} :spec {:host host
                            :port port
                            :password password}}))

  Identifier
  (get-id [_]
    service-id)

  Service
  (handler [_]
    (:redis @state)))

(defn make
  "Creates a redis component"
 [{:keys [service-id host port password]}]
 (->Redis (or service-id :redis) (atom {}) host port password))

(defmacro exec
  [service & body]
  `(car/wcar (handler ~service) ~@body))
