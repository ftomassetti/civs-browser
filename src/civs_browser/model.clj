(ns civs-browser.model
  ^{:author ftomassetti}
  (:use compojure.core)
  (:use hiccup.core)
  (:use hiccup.page)
  (:use ring.adapter.jetty)
  (:require
    [clojure.tools.cli :refer [parse-opts]]
    [clojure.string :as string]
    [civs.io :refer :all]
    [civs-browser.basic :refer :all]
    [clojure.edn :as edn]
    [clojure.set :refer [union]])
  (:gen-class))

(defn n-turns [history]
  (.size (keys (:facts history))))

(defn tribes-ids [history]
  (reduce (fn [acc game] (into acc (keys (:tribes game)))) #{} (vals (:game-snapshots history))))