(ns components.time-series.service
  (:require [time-series-storage.api :as time-series]
            [time-series-storage.postgres :as postgres]
            [time-series-storage.sql-server :as sql-server]
            [time-series-storage.mem :as memory])
  (:import [time_series_storage.postgres Postgres]
           [time_series_storage.sql_server SqlServer]
           [time_series_storage.mem Mem]
           [com.zaxxer.hikari HikariDataSource])
  (:use components.lifecycle.protocol))


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

(extend-type time_series_storage.mem.Mem

  Lifecycle
  (start [this system]
    )

  (stop [this system]
    )

  Service
  (handler [_]
    ))


(defn datasource
  [jdbc-config]
  (if (:pool jdbc-config)
    ;;pooled jdbc
    (let [max-pool (or (get-in jdbc-config [:pool :max-pool]) 20)
          cpds (doto (HikariDataSource.)
                 (.setJdbcUrl (str "jdbc:" (:subprotocol jdbc-config) ":" (:subname jdbc-config)))
                 (.setUsername (:user jdbc-config))
                 (.setPassword (:password jdbc-config))
                 (.addDataSourceProperty "maximumPoolSize" max-pool)
                 (.addDataSourceProperty "poolName" "TimeSeriesComponentHPool"))]
      {:datasource cpds})
    ;;plain jdbc
    jdbc-config))

(defn make
  "Creates a time-series component from a specified type"
 [{:keys [type config]}]
 (let [ds (datasource config)]
   (condp = type
     :mem (Mem. (atom {}))
     :postgres (Postgres. ds)
     :sql-server (SqlServer. ds))))
