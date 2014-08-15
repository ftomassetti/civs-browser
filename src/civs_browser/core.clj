(ns civs-browser.core
  (:use compojure.core)
  (:use hiccup.core)
  (:use hiccup.page)
  (:use ring.adapter.jetty)
  (:gen-class))

(defn view-layout [& content]
  (html
    (doctype :xhtml-strict)
    (xhtml-tag "en"
      [:head
       [:meta {:http-equiv "Content-type"
               :content "text/html; charset=utf-8"}]
       [:title "Civs-Browser"]]
      [:body content])))

(defn homepage []
  (view-layout
    [:h1 "Welcome to Civs-Browser!"]))

(defn parse-input [a b]
  [(Integer/parseInt a) (Integer/parseInt b)])

(defroutes app
  (GET "/" []
    (homepage)))

(defn -main [& args]

  (run-jetty #'civs-browser.core/app {:port 8080}))

