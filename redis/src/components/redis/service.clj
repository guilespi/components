(ns components.redis.service
  (:require [taoensso.carmine :as car :refer (wcar)])
  (:use components.lifecycle.protocol))


(defrecord Redis [state host port password]

  Lifecycle
  (stop [this system])
  (start [this system]
    (swap! state
           assoc :redis
           {:pool {} :spec {:host host
                            :port port
                            :password password}}))

  Service
  (handler [_]
    (:redis @state)))

(defn make
  "Creates a redis component"
 [{:keys [host port password]}]
 (->Redis (atom {}) host port password))


(defmacro exec
  [service & body]
  `(car/wcar (handler ~service) ~@body))
