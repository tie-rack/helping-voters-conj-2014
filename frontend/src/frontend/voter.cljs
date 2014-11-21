(ns frontend.voter
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async]
            [cljs.reader :as reader]
            [clojure.browser.repl :as repl])
  (:require-macros [cljs.core.async.macros :refer [go-loop alt!]]))

(defn search-view [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
               (dom/input #js {:ref "serial"})
               (dom/button #js {:onClick
                                #(om/update! app :serial-number
                                             (.-value (om/get-node owner "serial")))}
                           "Submit")))))

(defn mailing-url [serial-number]
  (str "http://localhost:8080/mailings/" serial-number))

(defn make-event-source [serial-number initial-data-chan updates-chan]
  (let [event-source (js/EventSource. (mailing-url serial-number))]
    (.addEventListener event-source "start"
                       (fn [e] (let [data (reader/read-string (.-data e))]
                                 (async/put! initial-data-chan data))))
    (.addEventListener event-source "update"
                       (fn [e] (let [data (reader/read-string (.-data e))]
                                 (async/put! updates-chan data))))))

(defn results-view [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [serial-number (:serial-number app)
            updates-chan (async/chan)
            initital-data-chan (async/chan)]
        (make-event-source serial-number initital-data-chan updates-chan)
        (go-loop []
          (alt!
            initital-data-chan ([data] (om/update! app :voter data))
            updates-chan ([data] (om/transact! app :voter #(merge % data))))
          (recur))))
    om/IRender
    (render [_]
      (dom/div nil
               (dom/h2 nil
                       (get-in app [:voter :firstname]) " "
                       (get-in app [:voter :lastname]))
               (dom/h3 nil (get-in app [:voter :status]))
               (dom/h4 nil (:serial-number app))))))

(def app-state (atom {:title "Hi"}))

(defn voter-view [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
               (dom/h1 nil (:title app))
               (if (:serial-number app)
                 (om/build results-view app)
                 (om/build search-view app))))))

(om/root voter-view app-state
  {:target (. js/document (getElementById "mailing-lookup"))})

(repl/connect "http://localhost:9000/repl")
