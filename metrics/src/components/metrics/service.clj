(ns components.metrics.service
  (:require
   [metrics.reporters.console :as console-reporter]
   [metrics.reporters.graphite :as graphite-reporter]
   [metrics.reporters :as rmng]
   [metrics.core]
   [metrics.jvm.core]
   [metrics.counters :as counters]
   [metrics.gauges :as gauges]
   [metrics.histograms :as histograms]
   [metrics.meters :as meters]
   [metrics.timers :as timers]
   [components.lifecycle.protocol]
   [components.metrics.protocol]
   [components.metrics.instrument :as instrument]))

(defn- start-console-reporter
  [registry report-freq]
  (rmng/start (console-reporter/reporter registry #{}) report-freq))

(defn- start-graphite-reporter
 [registry host port report-freq]
 (rmng/start (graphite-reporter/reporter registry {:host host :port port}) report-freq))

(defrecord SystemMonitor [state]
  components.metrics.protocol/Metrics

  (add-counter! [this id title]
    (let [registry (:metrics-registry @state)
          current  (:current/counters @state)]
      (swap! current assoc id (counters/counter registry title))))

  (counter? [this id]
    (get @(:current/counters @state) id))

  (inc-counter! [this id]
    (components.metrics.protocol/inc-counter! this id 1))

  (inc-counter! [this id value]
    (if-let [counter (get @(:current/counters @state) id)]
      (counters/inc! counter value)
      (throw (RuntimeException. (str "unknown counter " id)))))

  (dec-counter! [this id]
      (components.metrics.protocol/dec-counter! this id 1))

  (dec-counter! [this id value]
    (if-let [counter (get @(:current/counters @state) id)]
      (counters/dec! counter value)
      (throw (RuntimeException. (str "unknown counter " id)))))

  (add-gauge! [this id title function]
    (let [registry (:metrics-registry @state)
          current  (:current/gauges @state)]
      (swap! current assoc id (gauges/gauge-fn registry title function))))

  (gauge? [this id]
    (get @(:current/gauges @state) id))

  (add-histogram! [this id title]
    (let [registry (:metrics-registry @state)
          current  (:current/histograms @state)]
      (swap! current assoc id (histograms/histogram registry title))))

  (histogram? [this id]
    (get @(:current/histograms @state) id))

  (add-meter! [this id title]
    (let [registry (:metrics-registry @state)
          current  (:current/meters @state)]
      (swap! current assoc id (meters/meter registry title))))

  (meter? [this id]
    (get @(:current/meters @state) id))

  (mark-meter! [this id]
    (components.metrics.protocol/mark-meter! this id 1))

  (mark-meter! [this id value]
    (if-let [meter (get @(:current/meters @state) id)]
      (meters/mark! meter value)
      (throw (RuntimeException. (str "unknown meter " id)))))

  (update-histogram! [this id value]
    (if-let [histogram (get @(:current/histograms @state) id)]
      (histograms/update! histogram value)
      (throw (RuntimeException. (str "unknown histogram" id)))))

  (add-timer! [this id title]
    (let [registry (:metrics-registry @state)
          current  (:current/timers @state)]
      (swap! current assoc id (timers/timer registry title))))

  (timer? [this id]
    (get @(:current/meters @state) id))

  (clock-this! [this id subject]
    (if-let [timer (get @(:current/timers @state) id)]
      (if (fn? subject)
          (timers/time-fn! timer subject)
          (timers/time! timer subject))
      (throw (RuntimeException. (str "unknown timer " id)))))

  components.metrics.protocol/RegistryHolder
  (get-registry [this]
    (:metrics-registry @state))

  components.lifecycle.protocol/Lifecycle
  (start [this]
    (let [service-registry (metrics.core/new-registry)
          conf (:configuration @state)]
      (swap! state assoc
                         :metrics-registry service-registry
                         :current/counters   (atom {})
                         :current/gauges     (atom {})
                         :current/histograms (atom {})
                         :current/meters     (atom {})
                         :current/timers     (atom {}))
      (when (:instrument-jvm conf)
        (metrics.jvm.core/instrument-jvm service-registry))
      (when (:console-reporter conf)
      (when (get-in conf [:console-reporter :enabled])
        (start-console-reporter service-registry (get-in conf [:console-reporter :freq])))
      (instrument/instrument-all (:application-name conf) this)
      (when (get-in conf [:graphite-reporter :enabled])
        (start-graphite-reporter
          service-registry
          (get-in conf [:graphite-reporter :host])
          (get-in conf [:graphite-reporter :port])
          (get-in conf [:graphite-reporter :freq]))))))

  (stop [this]))

(defn make
  "Creates a monitor metrics server component"
  [config]
  (let [default-config {:application-name "dummy"
                        :instrument-jvm true
                        :graphite-reporter {:enabled false
                                            :host "localhost"
                                            :port 2003
                                            :freq 10}
                        :console-reporter {:enabled false
                                           :freq 10}}]

  (->SystemMonitor (atom {:configuration (merge-with
                                           #(if (map? %1) (merge %1 %2) %2)
                                           default-config config)}))))

(defrecord DummySystemMonitor [state]
  components.metrics.protocol/Metrics
  (add-counter! [this id title] true)
  (counter? [this id] true)
  (inc-counter! [this id] true)
  (inc-counter! [this id value] true)
  (dec-counter! [this id] true)
  (dec-counter! [this id value] true)
  (add-gauge! [this id title function] true)
  (gauge? [this id] true)
  (add-histogram! [this id title] true)
  (histogram? [this id] true)
  (add-meter! [this id title] true)
  (meter? [this id] true)
  (mark-meter! [this id] true)
  (mark-meter! [this id value] true)
  (update-histogram! [this id value] true)
  (add-timer! [this id title] true)
  (timer? [this id] true)
  (clock-this! [this id subject]
    (if (fn? subject)
      (subject)
      (eval subject)))
  components.metrics.protocol/RegistryHolder
  (get-registry [this]
    (:metrics-registry @state)))

(defn- make-dummy-system-monitor []
  (->DummySystemMonitor (atom {:metrics-registry (metrics.core/new-registry)})))

(def ^:dynamic *current-monitor* (make-dummy-system-monitor))

(defn monitor [] *current-monitor*)
