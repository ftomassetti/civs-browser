(ns civs-browser.views
  ^{:author ftomassetti}
  (:use compojure.core)
  (:use hiccup.core)
  (:use hiccup.page)
  (:use hiccup.element)
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

(defn tribes-homepage []
  (view-layout
    [:h1 "Civs-Browser: Tribes homepage"]
    (for [tribe-id (sort (tribes-ids history))]
      [:p "View " (link-to (str "tribe/" tribe-id) (str "Tribe " tribe-id))])))

(defn homepage []
  (view-layout
    [:h1 "Civs-Browser: Homepage"]
    [:p (str "No. turns: " (n-turns history))]
    [:p "View " (link-to "tribes" "Tribes")]))

(defn raw []
  (view-layout
    [:h1 "Civs-Browser: Raw view of the history file"]
    [:p (str history)]))
