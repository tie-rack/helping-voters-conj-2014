(defproject usps-ftp-queuer "0.1.0-SNAPSHOT"
  :description "Takes USPS uploads and queues lines"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.apache.ftpserver/ftpserver-core "1.0.6" :exclusions [org.slf4j/slf4j-api]]
                 [org.apache.ftpserver/ftplet-api "1.0.6"]
                 [org.slf4j/slf4j-simple "1.7.7"]
                 [org.clojure/data.csv "0.1.2"]
                 [org.clojure/tools.logging "0.3.0"]
                 [com.taoensso/carmine "2.7.0"]
                 [turbovote.imbarcode "0.1.4-SNAPSHOT"]])
