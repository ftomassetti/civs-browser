(defproject civs-browser "0.1.0-SNAPSHOT"
  :description "A web application to visualize the history files produced by civs"
  :url "https://github.com/ftomassetti/civs-browser"
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :main ^:skip-aot civs-browser.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
