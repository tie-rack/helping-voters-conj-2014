(ns api.mailing
  (:require [datomic.api :as d]
            [io.pedestal.interceptor :as i]
            [ring.util.response :as ring-resp]))

(defn find-by-serial-number [db serial-number]
  (some->> serial-number
           (d/q '[:find ?mailing
                  :in $ ?serial-number
                  :where [?mailing :mailing/serial-number ?serial-number]]
                db)
           ffirst
           (d/entity db)))

(i/defbefore lookup [ctx]
  (let [db (get-in ctx [:request :db])
        serial-number (get-in ctx [:request :path-params :serial-number])
        mailing (find-by-serial-number db serial-number)]
    (if mailing
      (assoc-in ctx [:request :mailing] mailing)
      (assoc ctx :response (ring-resp/not-found "Mailing not found")))))

(defn render [mailing]
  (let [status (->> mailing :scan/_mailing (sort-by :scan/time) last :scan/status)]
    {:firstname (:mailing/firstname mailing)
     :lastname (:mailing/lastname mailing)
     :serial-number (:mailing/serial-number mailing)
     :status status}))
