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
    (for [tribe-id (sort (groups-ids history))]
      [:p "View " (link-to (str "tribe/" tribe-id) (str "Tribe " tribe-id))])))

(defn homepage []
  (view-layout
    [:h1 "Civs-Browser: Homepage"]
    [:p (str "No. turns: " (n-turns history))]
    [:p "View " (link-to "tribes" "Tribes")]))

(defn- group-page-content [group-id]
  (let [ft (first-turn-for-group history group-id)
        lt (last-turn-for-group history group-id)]
  [:p (str "Alive from " ft " to " lt)]))

(defn tribe-page [tribe-id]
  (view-layout
    [:h1 (str "Civs-Browser: Group " tribe-id)]
    (if (exist-group? history tribe-id)
      (group-page-content tribe-id)
      [:p "This group does not exist"])))

(defn raw []
  (view-layout
    [:h1 "Civs-Browser: Raw view of the history file"]
    [:p (str history)]))
