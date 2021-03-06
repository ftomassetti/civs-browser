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

(defroutes basic-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (routes app-routes basic-routes)
    ; site creates an handler wrapped in some common middleware such as wrap-session, wrap-params and so on
    (handler/site)
    (wrap-base-url)))
