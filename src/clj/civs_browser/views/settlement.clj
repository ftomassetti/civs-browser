(ns civs-browser.views.settlement
  ^{:author ftomassetti}
  (:use compojure.core)
  (:use hiccup.core)
  (:use hiccup.page)
  (:use hiccup.element)
  (:use ring.adapter.jetty
        [incanter core stats charts])
  (:require
    [clojure.tools.cli :refer [parse-opts]]
    [clojure.string :as string]
    [civs.io :refer :all]
    [clojure.edn :as edn]
    [civs.model.core :refer :all]
    [civs.logic.demographics :refer :all]
    [civs-browser.basic :refer :all]
    [civs-browser.model :refer :all]
    [civs-browser.views.basic :refer :all])
  (:gen-class))

(defn- li-per-settlement [settlement-id]
  [:li
    [:span (link-to
             (str "settlement/" settlement-id)
             (str "Settlement " settlement-id " (" (first-turn-for-settlement history settlement-id)
               " - " (last-turn-for-settlement history settlement-id) " )"))]])

(defn settlements-view []
  (let [all-settlements (sort (settlements-ids history))
        [settlements-a settlements-bc] (split-at (/ (.size all-settlements) 3) all-settlements)
        [settlements-b settlements-c]   (split-at (/ (.size all-settlements) 3) settlements-bc)]
    (view-layout "Settlements homepage"
      [:h2 "All the settlements"]
      [:ul.settlements
       (for [settlement-id settlements-a]
         (li-per-settlement settlement-id))]
      [:ul.settlements
       (for [settlement-id settlements-b]
         (li-per-settlement settlement-id))]
      [:ul.settlements
       (for [settlement-id settlements-c]
         (li-per-settlement settlement-id))])))

(defn- settlement-page-content [id]
  (let [ft (first-turn-for-settlement history id)
        lt (last-turn-for-settlement history id)
        ts (sort (turns-in-which-settlement-is-populated history id))]
    (list
      [:p (str "Populated from " ft " to " lt)]
      [:h2 "Ownership"]
      [:p "Here we will list the owners of the settlement"]
      )))

(defn settlement-view [id]
  (view-layout (str "Settlement " id)
    (if (exist-settlement? history id)
      (settlement-page-content id)
      [:p "This settlement does not exist"])))