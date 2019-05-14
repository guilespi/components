(ns components.redis.service
  (:require [taoensso.carmine :as car :refer (wcar)])
  (:use components.lifecycle.protocol))


(defrecord Redis [state host port]

  Lifecycle
  (stop [this system])
  (start [this system]
    (swap! state
           assoc :redis
           {:pool {} :spec {:host host
                            :port port}}))

  Service
  (handler [_]
    (:redis @state)))

(defn make
  "Creates a redis component"
 [{:keys [host port]}]
 (->Redis (atom {}) host port))


(defmacro exec
  [service & body]
  `(car/wcar (handler ~service) ~@body))
