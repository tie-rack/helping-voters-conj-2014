(defproject frontend "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2322"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [om "0.8.0-alpha1"]
                 [com.cemerick/piggieback "0.1.3"]]
  :plugins [[lein-cljsbuild "1.0.3"]]
  :profiles {:dev {:source-paths ["dev-src"]}}
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]
                        :compiler {:output-to "main.js"
                                   :output-dir "out"
                                   :optimizations :whitespace
                                   :preamble ["react/react.min.js"]}}]})
