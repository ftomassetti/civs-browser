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

(defn groups-ids-in-game [game]
  (into #{} (keys (:tribes game))))

(defn games-in-which-group-is-alive [history group-id]
  (keys
    (filter
      (fn [[turn game]] (contains? (groups-ids-in-game game) group-id))
      (:game-snapshots history))))

(defn first-turn-for-group [history group-id]
  (apply min (games-in-which-group-is-alive history group-id)))

(defn last-turn-for-group [history group-id]
  (apply max (games-in-which-group-is-alive history group-id)))

(defn groups-ids [history]
  (reduce (fn [acc game] (into acc (groups-ids-in-game game))) #{} (vals (:game-snapshots history))))

(defn exist-group? [history tribe-id]
  (contains? (groups-ids history) tribe-id))