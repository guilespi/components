(ns components.lifecycle.protocol)

(defprotocol Lifecycle
  (start [this] [this system] "Start all life")
  (stop [this] [this system] "End all life"))
