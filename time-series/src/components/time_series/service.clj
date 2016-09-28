(ns components.time-series.service
  (:require [time-series-storage.api :as time-series]
            [time-series-storage.postgres :as postgres]
            [time-series-storage.sql-server :as sql-server])
  (:import [time_series_storage.postgres Postgres]
           [time_series_storage.sql_server SqlServer]
           [com.jolbox.bonecp BoneCPDataSource])
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


(defn make-pool
  [config]
  (let [partitions (or (get-in config [:pool :partitions]) 3)
        min-pool (or (get-in config [:pool :min-pool]) 5)
        max-pool (or (get-in config [:pool :max-pool]) 20)
        cpds (doto (BoneCPDataSource.)
               (.setJdbcUrl (str "jdbc:" (:subprotocol config) ":" (:subname config)))
               (.setUsername (:user config))
               (.setPassword (:password config))
               (.setMinConnectionsPerPartition (inc (int (/ min-pool partitions))))
               (.setMaxConnectionsPerPartition (inc (int (/ max-pool partitions))))
               (.setPartitionCount partitions)
               (.setStatisticsEnabled true)
               ;; Test connections default is 240 minutes
               (.setIdleConnectionTestPeriodInMinutes (or (get-in config [:pool :test-period]) 240))
               ;; Default idle is 60 minutes
               (.setIdleMaxAgeInMinutes (or (get-in config [:pool :max-age]) 60))
               ;;Query to avoid connections from dying (varies on database)
               (.setConnectionTestStatement (or (get-in config [:pool :test-statement])
                                                "/* ping *\\/ SELECT 1")))]
    {:datasource cpds}))

(defn make
  "Creates a time-series component from a specified type"
 [{:keys [type config]}]
 (condp = type
   :postgres (Postgres. (if (:pool config)
                          (make-pool config)
                          config))
   :sql-server (SqlServer. config)))
