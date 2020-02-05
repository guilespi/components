(ns components.blob-storage.service
  (:require [blob-storage.api :as blob-api]
            [blob-storage.postgres :as postgres]
            [blob-storage.sql-server :as sql-server]
            [blob-storage.mem :as memory])
  (:import [blob_storage.postgres Postgres]
           [blob_storage.sql_server SqlServer]
           [blob_storage.mem Mem]
           [com.zaxxer.hikari HikariDataSource])
  (:use components.lifecycle.protocol))

(extend-type blob_storage.postgres.Postgres
  Lifecycle
  (start [this system]
    (blob-api/init-schema! this))

  (stop [this system])

  Service
  (handler [_]
    ))

(extend-type blob_storage.sql_server.SqlServer
  Lifecycle
  (start [this system]
    (blob-api/init-schema! this))

  (stop [this system])

  Service
  (handler [_]
    ))

(extend-type blob_storage.mem.Mem
  Lifecycle
  (start [this system]
    )

  (stop [this system])

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
                 (.addDataSourceProperty "poolName" "BlobStorageComponentsHPool"))]
      {:datasource cpds})
    ;;plain jdbc
    jdbc-config))

(defn make
  "Creates a blob-storage component from a specified type"
  [{:keys [type config]}]
  (let [ds (datasource config)]
    (condp = type
      :mem (Mem. (atom {}))
      :postgres (Postgres. ds)
      :sql-server (SqlServer. ds))))
