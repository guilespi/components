(ns components.metrics.protocol
  (:require [metrics.timers :as timers]))

(defprotocol Metrics
  (add-counter! [m id title])
  (add-gauge! [m id title function])
  (add-histogram! [m id title])
  (add-meter! [m id title])
  (add-timer! [m id title])
  (inc-counter! [m id value] [m id])
  (dec-counter! [m id value] [m id])
  (update-histogram! [m id value])
  (mark-meter! [m id value] [m id])
  (counter? [m id])
  (gauge? [m id])
  (histogram? [m id])
  (meter? [m id])
  (timer? [m id])
  (get-timer [m id]))

(defprotocol RegistryHolder
  (get-registry [m]))

(defmacro clock-this!
  [monitor timer-id subject]
  `(if (get-timer ~monitor ~timer-id)
     (timers/time! (get-timer ~monitor ~timer-id) ~subject)
     ~subject))
