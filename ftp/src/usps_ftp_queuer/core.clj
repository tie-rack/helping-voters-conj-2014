(ns usps-ftp-queuer.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [taoensso.carmine :as car]
            [taoensso.carmine.message-queue :as car-mq]
            [turbovote.imbarcode :as imb])
  (:import [org.apache.ftpserver FtpServerFactory]
           [org.apache.ftpserver.listener ListenerFactory]
           [org.apache.ftpserver.usermanager PropertiesUserManagerFactory]
           [org.apache.ftpserver.ftplet DefaultFtplet]))

(def work-queue "usps-scans")

(defn row->map [row]
  (let [[facility status time routing-code structure-digits] row
        time (java.util.Date. time)
        routing-code (string/trim routing-code)]
    {:facility-zip facility
     :status status
     :scan-time time
     :imb-data (imb/split-structure-digits
                (str structure-digits routing-code))}))

(defn process-file [path]
  (->> path
       io/reader
       csv/read-csv
       (map row->map)))

(def enqueue-lines-ftplet
  (proxy [DefaultFtplet] []
    (onUploadEnd [session request]
      (let [user-home (-> session .getUser .getHomeDirectory)
            curr-dir (-> session
                         .getFileSystemView
                         .getWorkingDirectory
                         .getAbsolutePath)
            filename (str user-home curr-dir (.getArgument request))]
        (car/wcar {}
                  (doseq [entry (process-file filename)]
                    (car-mq/enqueue work-queue entry))))
      nil)))

(def user-file
  (-> "users.properties"
      io/resource
      io/as-file))

(def user-manager-factory
  (doto (PropertiesUserManagerFactory.)
    (.setFile user-file)))

(def user-manager
  (.createUserManager user-manager-factory))

(def listener-factory
  (doto (ListenerFactory.)
    (.setPort 2221)))

(def listener
  (.createListener listener-factory))

(def server-factory
  (doto (FtpServerFactory.)
    (.addListener "default" listener)
    (.setUserManager user-manager)
    (.setFtplets (java.util.HashMap. {"enqueue-lines" enqueue-lines-ftplet}))))

(def server
  (.createServer server-factory))

(defn -main []
  (.start server))
