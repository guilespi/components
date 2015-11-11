(ns components.blob-storage.service
  (:require [blob-storage.api :as blob-api]
            [blob-storage.postgres :as postgres])
  (:import [blob_storage.postgres Postgres])
  (use components.lifecycle.protocol))

(extend-type blob_storage.postgres.Postgres
  Lifecycle
  (start [this system]
    (blob-api/init-schema! this))

  (stop [this system])

  Service
  (handler [_]
    ))

(defn make
  "Creates a blob-storage component from a specified type"
  [{:keys [type config]}]
  (condp = type
    :postgres (Postgres. config)))
