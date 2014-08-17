(defproject civs-browser "0.1.0-SNAPSHOT"
  :description "A web application to visualize the history files produced by civs"
  :url "https://github.com/ftomassetti/civs-browser"
  :dependencies [
                  [org.clojure/clojure "1.6.0"]
                  [civs "0.2.0-SNAPSHOT"]
                  [com.github.lands/lands-java-lib "0.3-SNAPSHOT"]
                  [org.clojure/tools.cli "0.3.1"]
                  [javax.servlet/servlet-api "2.5"]
                  [ring/ring-core "1.3.0"]
                  [ring/ring-devel "1.3.0"]
                  [ring/ring-jetty-adapter "1.3.0"]
                  [compojure "1.1.8"]
                ]
  :dev-dependencies
                [[lein-run "1.0.1-SNAPSHOT"]]
  :main ^:skip-aot civs-browser.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :repositories {"sonartype snapshots" "https://oss.sonatype.org/content/repositories/snapshots"})
