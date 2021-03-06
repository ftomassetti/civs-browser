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
    [civs.model.history :refer :all]
    [civs-browser.basic :refer :all]
    [clojure.edn :as edn]
    [clojure.set :refer [union]])
  (:gen-class))

;#########################################
; Turns
;#########################################


;#########################################
; Game
;#########################################

(defn ordered-games []
  (map (fn [t] (get (:game-snapshots history) t)) (turns history)))

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

(defn turns-in-which-group-is-alive [history group-id]
  (sort (filter
    (fn [turn] (let
                 [gr (group-at history turn group-id)]
                 (if (nil? gr) false (alive? gr))))
    (turns history))))

(defn group-position-at [history turn group-id]
  (let [ga (game-at history turn)
        gr (group-at history turn group-id)]
    (.position gr)))

(defn group-positions-in-time [history group-id]
  (into {}
    (map
      (fn [turn] [turn (group-position-at history turn group-id)])
      (turns-in-which-group-is-alive history group-id))))

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
  (apply min (turns-in-which-group-is-alive history group-id)))

(defn last-turn-for-group [history group-id]
  (apply max (turns-in-which-group-is-alive history group-id)))

(defn groups-ids [history]
  (reduce (fn [acc game] (into acc (groups-ids-in-game game))) #{} (vals (:game-snapshots history))))

(defn exist-group? [history group-id]
  (contains? (groups-ids history) group-id))

(defn group-popdata-in-time [history group-id]
  (map #(tribe-total-pop (group-at history % group-id)) (turns-in-which-group-is-alive history group-id)))

(defn group-name [history group-id]
  (let [t (last-turn-for-group history group-id)
        g (group-at history t group-id)]
    (.name g)))

;#########################################
; Settlements
;#########################################

(defn settlements-ids-in-game [game]
  (into #{} (keys (:settlements game))))

(defn settlements-ids [history]
  (reduce (fn [acc game] (into acc (settlements-ids-in-game game))) #{} (vals (:game-snapshots history))))

(defn exist-settlement? [history settlement-id]
  (contains? (settlements-ids history) settlement-id))

(defn settlement-at [history turn id]
  (get (:settlements (game-at history turn)) id))

(defn turns-in-which-settlement-is-populated [history id]
  (sort (filter
          (fn [turn] (let
                       [settl (settlement-at history turn id)]
                       (if (nil? settl) false (not (ghost-city? (game-at history turn) id)))))
          (turns history))))

(defn first-turn-for-settlement [history id]
  (apply min (turns-in-which-settlement-is-populated history id)))

(defn last-turn-for-settlement [history id]
  (apply max (turns-in-which-settlement-is-populated history id)))

(defn settlement-name [history settlement-id]
  (let [t (last-turn-for-settlement history settlement-id)
        g (settlement-at history t settlement-id)]
    (.name g)))

;#########################################
; Facts
;#########################################

(defn facts-by-turn-and-group [history turn group-id]
  (let [facts-per-turn (get (:facts history) turn)
        facts-per-turn-and-group (filter #(= group-id (:tribe %)) facts-per-turn)]
    facts-per-turn-and-group))

;#########################################
; Misc
;#########################################

(defn popdata []
  (map #(game-total-pop %) (ordered-games)))
