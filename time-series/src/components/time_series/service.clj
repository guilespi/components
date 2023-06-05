(ns components.time-series.service
  (:require [time-series-storage.api :as time-series]
            [time-series-storage.postgres :as postgres]
            [time-series-storage.sql-server :as sql-server]
            [time-series-storage.mem :as memory])
  (:import [time_series_storage.postgres Postgres]
           [time_series_storage.sql_server SqlServer]
           [time_series_storage.mem Mem]
           [com.jolbox.bonecp BoneCPDataSource])
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
    (let [partitions (or (get-in jdbc-config [:pool :partitions]) 3)
          min-pool (or (get-in jdbc-config [:pool :min-pool]) 5)
          max-pool (or (get-in jdbc-config [:pool :max-pool]) 20)
          cpds (doto (BoneCPDataSource.)
                     (.setJdbcUrl (str "jdbc:" (:subprotocol jdbc-config) ":" (:subname jdbc-config)))
                     (.setUsername (:user jdbc-config))
                     (.setPassword (:password jdbc-config))
                     (.setMinConnectionsPerPartition (inc (int (/ min-pool partitions))))
                     (.setMaxConnectionsPerPartition (inc (int (/ max-pool partitions))))
                     (.setPartitionCount partitions)
                     (.setStatisticsEnabled true)
                     ;; Test connections default is 240 minutes
                     (.setIdleConnectionTestPeriodInMinutes (or (get-in jdbc-config [:pool :test-period]) 240))
                     ;; Default idle is 60 minutes
                     (.setIdleMaxAgeInMinutes (or (get-in jdbc-config [:pool :max-age]) 60))
                     ;;Query to avoid connections from dying (varies on database)
                     (.setConnectionTestStatement (or (get-in jdbc-config [:pool :test-statement])
                                                      "/* ping */ SELECT 1")))] ;;this statement should work with mysql,
      ;; sql-server, postgresql h2 and sqlite
      ;; be careful

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
