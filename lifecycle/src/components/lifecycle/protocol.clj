(ns components.lifecycle.protocol)

(defprotocol Lifecycle
  (start [this] [this system] "Start all life")
  (stop [this] [this system] "End all life"))

(defprotocol Service
  (handler [this] "Retrieves service handler"))

(defprotocol Identifier
  (get-id [this] "Retrieves service identifier"))
