(ns components.time-series.service
  (:require [time-series-storage.api :as time-series]
            [time-series-storage.postgres :as postgres]
            [time-series-storage.sql-server :as sql-server])
  (:import [time_series_storage.postgres Postgres]
           [time_series_storage.sql_server SqlServer])
  (use components.lifecycle.protocol))


(extend-type time_series_storage.postgres.Postgres

  Lifecycle
  (start [this system]
    (time-series/init-schema! this))

  (stop [this system]
    )

  Service
  (handler [_]
    ))

(extend-type time_series_storage.sql_server.SqlServer

  Lifecycle
  (start [this system]
    )

  (stop [this system]
    )

  Service
  (handler [_]
    ))


(defn make
  "Creates a time-series component from a specified type"
 [{:keys [type config]}]
 (condp = type
   :postgres (Postgres. config)
   :sql-server (SqlServer. config)))
