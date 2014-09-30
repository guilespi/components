(ns components.datomic.service
  (require [datomic.api :as d])
  (use components.lifecycle.protocol))

;;TODO:this should be a multimethod for mutiple storage support
(defn- make-uri
  [db host port]
  (format "datomic:sql://%s?jdbc:postgresql://%s:%s/%s"
          db host port))

(defrecord Datomic [state db host port]
  Lifecycle
  (stop [this system]
    )
  (start [this system]
    (swap! state
           assoc :datomic
           (d/connect (make-uri db host port)))))

(defn make
  "Creates a datomic db component"
 [{:keys [db host port]}]
 (->Datomic (atom {}) db host port))
