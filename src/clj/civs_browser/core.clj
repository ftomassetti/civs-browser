(ns civs-browser.core
  ^{:author ftomassetti}
  (:use compojure.core)
  (:use hiccup.core)
  (:use hiccup.page)
  (:use ring.adapter.jetty)
  (:require
    [clojure.tools.cli :refer [parse-opts]]
    [clojure.string :as string]
    [civs.io :refer :all]
    [civs.model.core :refer :all]
    [clojure.edn :as edn]
    [civs-browser.basic :refer :all]
    [civs-browser.model :refer :all]
    [civs-browser.views :refer :all])
  (:gen-class))

(def cli-options
  ;; An option with a required argument
  [
    ["-f" "--history-filename HISTORY_FILENAME" "History file to be used"]
    ["-h" "--help"]
    ["-w" "--worlds-dir WORLD_DIR" "Dir containing world files"
     :default ""]
  ])

(defn parse-input [a b]
  [(Integer/parseInt a) (Integer/parseInt b)])

(defroutes app-routes
  (GET "/" []
    (homepage))
  (GET "/worldpop.png" []
    (world-pop-plot))
  (GET "/raw" []
    (raw))
  (GET "/tribes" []
    (tribes-homepage))
  (GET "/ancient-map.png" []
    (world-ancient-map-view))
  (GET "/prosperity-map-gh.png" []
    (world-prosperity-map-view :gathering-and-hunting))
  (GET "/prosperity-map-agr.png" []
    (world-prosperity-map-view :agriculture))
  (GET ["/group/:id/movements.png", :id #"[0-9]+"] [id]
    (tribe-movements-ancient-map-view (read-string id)))
  (GET ["/group/:id", :id #"[0-9]+"] [id]
    (group-page (read-string id))))

(defn failure [msg]
  (binding [*out* *err*]
    (println "Error:" msg)
    (println "")
    (println "Use -h for help")
    (println "Exit."))
  (System/exit 1))

(defn usage [options-summary]
  (println (->> ["This program run a browser to explore results of simulations of civilizations evolution and struggling"
                 ""
                 "Usage: [lein run] civs-browser [options]"
                 ""
                 "Options:"
                 options-summary
                 ""
                 "Feel free to ask all possible questions on https://github.com/ftomassetti/civs-browser (just open an issue!)"]
             (string/join \newline)))
  (System/exit 0))

(defn load-history-file-edn [history-filename]
  (let [ edn-str (slurp history-filename)
         resolver (dir-lists-resolver [""])
         history (from-serialized-str edn-str {:resolver resolver})]
    history))

(defn error-throwing-dir-lists-resolver
  "Resolver using cache, adding extension and throwing exception when not found"
  [world-dir]
  (let [wrapped (dir-lists-resolver [world-dir])
        cache (atom {})]
    (fn [filename]
      (if (get (deref cache) filename)
        (get (deref cache) filename)
        (let [name-from-wrapped (wrapped (str filename ".world"))]
          (if (nil? name-from-wrapped)
            (failure (str "Cannot load " filename))
            (do
              (let [res (load-world name-from-wrapped)]
                (swap! cache assoc filename res)
                res))))))))

(defn load-history-file-fressian [history-filename worlds-dir]
  (load-simulation-result-fressian history-filename
    (error-throwing-dir-lists-resolver worlds-dir)))

(defn run [history]
  (set-history history)
  (run-jetty #'civs-browser.core/app-routes {:port 8080}))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (nil? (:history-filename options))
      (failure "History file to be used not specified (option -f missing)")
      (:help options)
      (usage summary)
      errors (failure errors))
    (let [history (load-history-file-fressian (:history-filename options) (:worlds-dir options))]
      (run history))))

; (set-history (load-history-file-fressian "examples-history/w77_100turns.history" "examples-worlds"))

; (javax.imageio.ImageIO/read (java.io.File. "examples-maps/ancient_map_seed_77.png"))