(ns components.metrics.middleware
  (:require
   [metrics.ring.instrument :as ring-metrics]
   [components.metrics.protocol :as metrics]
   [components.metrics.service :as service]
))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- normalize-uri [uri]
  "Clears uri from parameters and perform some weed out for urls like
   streets/:street/find"
  (-> (re-find #"[^\?]*" uri)
      (clojure.string/replace #"(streets).*$" "streets")))

(defn url-middleware
  "Instrument a timer for each url call"
  [handler system-monitor application-name]
    (fn [request]
      (let [uri (normalize-uri (:uri request))]
        (when-not (metrics/timer? system-monitor uri)
          (metrics/add-timer! system-monitor uri [application-name "web-times" uri]))
        (when-not (metrics/meter? system-monitor uri)
          (metrics/add-meter! system-monitor uri [application-name "web-meter" uri]))
        (metrics/mark-meter! system-monitor uri)
        (metrics/clock-this! system-monitor uri
          (binding [service/*current-monitor* system-monitor]
            (handler request))))))

(defn ring-middleware [handler system-monitor]
  (ring-metrics/instrument handler (metrics/get-registry system-monitor)))

