(ns api.service
  (:require [io.pedestal.http :as bootstrap]
            [io.pedestal.http.sse :as sse]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor :as i]
            [ring.util.response :as ring-resp]
            [clojure.core.async :as async]
            [turbovote.datomic-toolbox :as db]
            [api.mailing :as m]
            [api.scan-processor :as sp]))

(i/defon-request acquire-db
  [request]
  (assoc request :db (db/db)))

(defn mailing-events [event-chan ctx]
  (let [serial-number (get-in ctx [:request :mailing :mailing/serial-number])
        updates-channel (async/chan)]
    (async/sub sp/mailing-pub serial-number updates-channel)
    (async/put! event-chan {:name "start" :data (pr-str (m/render (get-in ctx [:request :mailing])))})
    (async/go-loop [update (async/<! updates-channel)]
      (let [data (select-keys update [:status :time])]
        (if (async/>! event-chan {:name "update" :data (pr-str data)})
          (recur (async/<! updates-channel))
          (async/unsub sp/mailing-pub serial-number updates-channel))))))

(defn pong [_]
  (ring-resp/response "pong"))

(defroutes routes
  [[["/"
     ^:interceptors [acquire-db]
     ["/ping"
      {:get pong}]
     ["/mailings/:serial-number"
      {:get [:mailing-events
             ^:interceptors [m/lookup]
             (sse/start-event-stream mailing-events)]}]]]])

(def service {:env :prod
              ::bootstrap/routes routes
              ::bootstrap/resource-path "/public"
              ::bootstrap/type :jetty
              ::bootstrap/port 8080
              ::bootstrap/allowed-origins (constantly true)})
