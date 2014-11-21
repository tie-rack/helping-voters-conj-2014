(ns dev.repl
  (:require [cljs.repl :as repl]
            [cljs.repl.server :as server]
            [cljs.repl.browser :as browser]))

(def env (browser/repl-env))

(defn start-repl []
  (cemerick.piggieback/cljs-repl
   :repl-env env)
  (server/dispatch-on :get
                      (fn [{:keys [path]} _ _] (.endsWith path ".css"))
                      browser/send-static))
