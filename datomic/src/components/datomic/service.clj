(ns components.datomic.service
  (require [datomic.api :as d])
  (use components.lifecycle.protocol))

(defmulti make-uri (fn [storage options] storage))

(defmethod make-uri :postgres
  [storage {:keys [db host port]}]
  (format "datomic:sql://%s?jdbc:postgresql://%s:%s/%s"
          db host port db))

(defmethod make-uri :sql-server
  [storage {:keys [db host port user password]}]
  (format "datomic:sql://%s?jdbc:sqlserver://%s:%s;DatabaseName=%s;sendStringParametersAsUnicode=false;Username=%s;Password=%s"
          db host port db user password))


(defmethod make-uri :cassandra
  [storage {:keys [db host port user password]}]
  (format "datomic:cass://%s:%s/datomic.datomic/%s?ssl=&password=%s&user=%s"
          host port
          db user password))

(defmethod make-uri :mem
  [storage {:keys [db-name]}]
  (format "datomic:mem://%s"
          db-name))

(defprotocol Uri
  (get-uri [this] "Returns unique URI identifier for datomic storage"))

(defrecord Datomic [state uri]
  Lifecycle
  (stop [this system]
    )
  (start [this system]
    (swap! state
           assoc :datomic
           (d/connect uri)))
  Uri
  (get-uri [_]
    uri)

  Service
  (handler [_]
    (:datomic @state)))

(defn make
  "Creates a datomic db component"
 [storage options]
 (->Datomic (atom {}) (make-uri storage options)))
