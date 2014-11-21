(ns dev.db
  (:require [clojure.edn :as edn]
            [turbovote.datomic-toolbox :as db]
            [datomic.api :as d]
            [turbovote.resource-config :refer [config]]))

(defn read-resource [resource-path]
  (->> resource-path
       clojure.java.io/resource
       slurp
       (edn/read-string {:readers *data-readers*})))

(def seed-tx-data
  (read-resource "seed.edn"))

(defn seed-db []
  (db/transact seed-tx-data))

(defn reset-db! []
  (d/delete-database (config :datomic :uri))
  (db/initialize)
  (seed-db))

(defn -main []
  (println "Resetting the database")
  (reset-db!)
  (println "We did it!")
  (System/exit 0))
