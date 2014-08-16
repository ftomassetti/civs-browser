(ns civs-browser.views
  ^{:author ftomassetti}
  (:use compojure.core)
  (:use hiccup.core)
  (:use hiccup.page)
  (:use ring.adapter.jetty)
  (:require
    [clojure.tools.cli :refer [parse-opts]]
    [clojure.string :as string]
    [civs.io :refer :all]
    [clojure.edn :as edn]
    [civs-browser.basic :refer :all]
    [civs-browser.model :refer :all])
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
    [:h1 "Welcome to Civs-Browser!"]
    [:p (str "No. turns: " (n-turns history))]
    [:p (str "Facts: " (.size (:facts history)))]))

(defn raw []
  (view-layout
    [:h1 "Civs-Browser: raw view of the history file"]
    [:p (str history)]))
