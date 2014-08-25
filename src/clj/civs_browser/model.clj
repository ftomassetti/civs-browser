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
    [civs.model.core :refer :all]
    [civs-browser.basic :refer :all]
    [clojure.edn :as edn]
    [clojure.set :refer [union]])
  (:gen-class))

;#########################################
; Turns
;#########################################

(defn n-turns [history]
  (.size (keys (:facts history))))

(defn turns
  ([] (turns history))
  ([history] (sort (keys (:game-snapshots history)))))

(defn exist-turn? [history turn]
  (not (nil? (some #{turn} (turns history)))))

;#########################################
; Game
;#########################################

(defn game-at [history turn]
  (get (:game-snapshots history) turn))

(defn ordered-games []
  (map (fn [t] (get (:game-snapshots history) t)) (turns)))

;#########################################
; World
;#########################################

(defn world [history]
  (:world (first (vals (:game-snapshots history)))))

(defn width [history]
  (-> (world history) .getDimension .getWidth))

(defn height [history]
  (-> (world history) .getDimension .getHeight))

;#########################################
; Groups
;#########################################

(defn groups-ids-in-game [game]
  (into #{} (keys (:tribes game))))

(defn games-in-which-group-is-alive [history group-id]
  (keys
    (filter
      (fn [[turn game]] (contains? (groups-ids-in-game game) group-id))
      (:game-snapshots history))))

(defn group-at [history turn group-id]
  (get (:tribes (game-at history turn)) group-id))

(defn group-position-at [history turn group-id]
  (let [ga (game-at history turn)
        gr (group-at history turn group-id)]
    (.position gr)))

(defn group-positions-in-time [history group-id]
  (into {}
    (map
      (fn [turn] [turn (group-position-at history turn group-id)])
      (games-in-which-group-is-alive history group-id))))

(defn distinct-group-positions-in-time [history group-id]
  (let [pos-in-time (sort (group-positions-in-time history group-id))
        current-mov (first pos-in-time)
        current-pos (get current-mov 1)
        next-mov (rest pos-in-time)]
    (get (reduce
           (fn [[current-pos movements-done] mov]
             (let [target-pos (get mov 1)]
               (if (= current-pos target-pos)
                 [current-pos movements-done]
                 [target-pos (conj movements-done mov)])))
           [current-pos [current-mov]] next-mov) 1)))

(defn first-turn-for-group [history group-id]
  (apply min (games-in-which-group-is-alive history group-id)))

(defn last-turn-for-group [history group-id]
  (apply max (games-in-which-group-is-alive history group-id)))

(defn groups-ids [history]
  (reduce (fn [acc game] (into acc (groups-ids-in-game game))) #{} (vals (:game-snapshots history))))

(defn exist-group? [history tribe-id]
  (contains? (groups-ids history) tribe-id))

;#########################################
; Misc
;#########################################

(defn popdata []
  (map #(game-total-pop %) (ordered-games)))
