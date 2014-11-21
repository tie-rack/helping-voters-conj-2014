(ns api.scan-processor
  (:require [api.mailing :as m]
            [datomic.api :as d]
            [clojure.core.async :as async]
            [turbovote.datomic-toolbox :as db]
            [taoensso.carmine :as car]
            [taoensso.carmine.message-queue :as car-mq]))

(def work-queue "usps-scans")

(def updates-channel (async/chan (async/sliding-buffer 10)))

(def mailing-pub
  (async/pub updates-channel #(get-in % [:mailing :mailing/serial-number])))

(defn ->tx-data [scan mailing]
  [{:db/id (d/tempid :mailing-api)
    :scan/mailing  mailing
    :scan/status (:status scan)
    :scan/time (:scan-time scan)}])

(defn process-scan [scan]
  (let [db (db/db)
        serial-number (get-in scan [:imb-data :6-digit-mailer :serial-number])
        mailing (m/find-by-serial-number db serial-number)
        {:keys [scan-time status]} scan]
    (async/put! updates-channel {:mailing mailing
                                 :time scan-time
                                 :status status})
    (db/transact (->tx-data scan (:db/id mailing)))))

(defn work! []
  (car-mq/worker {} work-queue
                 {:handler (fn [{:keys [message attempt]}]
                             (println "Received" message)
                             (process-scan message)
                             {:status :success})
                  :eoq-backoff-ms 1
                  :throttle-ms 0}))
