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
    [:span (link-to (str "settlment/" settlement-id) (str "Settlement " settlement-id))]])

(defn settlements-view []
  (let [all-settlements (sort (settlements-ids history))]
    (view-layout "Settlements homepage"
      [:h2 "All the settlements"]
      [:ul.groups
       (for [settlement-id all-settlements]
         (li-per-settlement settlement-id))])))