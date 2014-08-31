(defproject civs-browser "0.1.0-SNAPSHOT"
  :description "A web application to visualize the history files produced by civs"
  :url "https://github.com/ftomassetti/civs-browser"
  :license {:name "The Apache Software License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
                  [org.clojure/clojure "1.6.0"]
                  [civs "0.2.2-SNAPSHOT"]
                  [com.github.lands/lands-java-lib "0.3-SNAPSHOT"]
                  [org.clojure/tools.cli "0.3.1"]
                  [javax.servlet/servlet-api "2.5"]
                  [ring/ring-core "1.3.0"]
                  [ring/ring-jetty-adapter "1.3.0"]
                  [ring-server "0.3.1"]
                  [compojure "1.1.8"]
                  [org.clojure/clojurescript "0.0-2311"]
                  [jayq "2.5.2"]
                  [incanter "1.2.3-SNAPSHOT"]
                  [animated-gif-clj "0.1.0-SNAPSHOT"]
                ]
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-ring "0.8.10"]]
  :ring {:handler civs-browser.handler/app
         :init    civs-browser.handler/init
         :destroy civs-browser.handler/destroy}
  :dev-dependencies
                [[lein-run "1.0.1-SNAPSHOT"]]
  :main ^:skip-aot civs-browser.core
  :target-path "target/%s"
  :profiles
    {
      :uberjar { :aot :all}
     :production {:ring {:open-browser? false, :stacktraces? false, :auto-reload? false}}
     :dev {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.3.0"]]}
     }
  :repositories {"sonartype snapshots" "https://oss.sonatype.org/content/repositories/snapshots"}
  :source-paths ["src/clj"]
  :cljsbuild
  {:builds
   [{:source-paths ["src/cljs"],
     :id "main",
     :compiler
     {:optimizations :simple,
      :output-to "resources/js/cljs.js",
      :pretty-print true}}]}
  )
