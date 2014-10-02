(ns components.metrics.protocol)

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
  (clock-this! [m id function])
  (counter? [m id])
  (gauge? [m id])
  (histogram? [m id])
  (meter? [m id])
  (timer? [m id]))

(defprotocol RegistryHolder
  (get-registry [m]))

