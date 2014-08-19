(ns civs-browser.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [civs-browser.core :refer [app-routes]]))

(defn init []
  (println "civs-browser is starting"))

(defn destroy []
  (println "civs-browser is shutting down"))

(def app
  (-> (routes app-routes)
    (handler/site)
    (wrap-base-url)))
