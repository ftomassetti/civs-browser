(ns civs-browser.repl
  (:use civs-browser.handler
        ring.server.standalone
        [ring.middleware file-info file]
        civs-browser.basic
        civs-browser.model
        civs.model
        civs-browser.core)
  (:require [civs.io :refer :all]))

(defonce server (atom nil))

(defn get-handler []
  ;; #'app expands to (var app) so that when we reload our code,
  ;; the server is forced to re-resolve the symbol in the var
  ;; rather than having its own copy. When the root binding
  ;; changes, the server picks it up without having to restart.
  (-> #'app
    ; Makes static assets in $PROJECT_DIR/resources/public/ available.
    (wrap-file "resources")
    ; Content-Type, Content-Length, and Last Modified headers for files in body
    (wrap-file-info)))

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (if port (Integer/parseInt port) 8080)]
    (set-history (load-history-file-fressian "examples-history/w77_100turns.history" "examples-worlds"))
    (let [turns (sort (keys (:game-snapshots history)))
          ordered-games (map (fn [t] (get (:game-snapshots history) t)) turns)]
      (def popdata-byyear (map (fn [[t g]] [t (game-total-pop g)]) (:game-snapshots history))))
    (reset! server
      (serve (get-handler)
        {:port port
         :init init
         :auto-reload? true
         :destroy destroy
         :join true}))
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-server []
  (.stop @server)
  (reset! server nil))