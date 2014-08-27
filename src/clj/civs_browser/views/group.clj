(ns civs-browser.views.group
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

(import com.github.lands.draw.AncientMapDrawer)
(import java.io.ByteArrayOutputStream)
(import java.io.ByteArrayInputStream)
(import java.awt.image.BufferedImage)
(import java.awt.RenderingHints)
(import java.awt.Color)
(import javax.imageio.ImageIO)

(defn- group-page-content [group-id]
  (let [ft (first-turn-for-group history group-id)
        lt (last-turn-for-group history group-id)]
    [:p (str "Alive from " ft " to " lt)]
    (image (str "/group/" group-id "/movements.png"))))

(defn group-view [group-id]
  (view-layout (str "Group "group-id)
    (if (exist-group? history group-id)
      (group-page-content group-id)
      [:p "This group does not exist"])))

(defn groups-view []
  (let [all-groups (sort (groups-ids history))
        [groups-a groups-bc] (split-at (/ (.size all-groups) 3) all-groups)
        [groups-b groups-c]  (split-at (/ (.size all-groups) 3) groups-bc)]
    (view-layout "Groups homepage"
      [:h2 "All the groups"]
      [:ul.groups
       (for [tribe-id groups-a]
         [:li
          [:span (link-to (str "group/" tribe-id) (str "Group " tribe-id))]])]
      [:ul.groups
       (for [tribe-id groups-b]
         [:li
          [:span (link-to (str "group/" tribe-id) (str "Group " tribe-id))]])]
      [:ul.groups
       (for [tribe-id groups-c]
         [:li
          [:span (link-to (str "group/" tribe-id) (str "Group " tribe-id))]])])))

(defn- graded-colors [len]
  (let [values (map #(/ % (float len)) (range len))
        colors (map #(java.awt.Color. (int (* % 255)) 0 (- 255 (int (* % 255)))) values)]
    colors))

(defn group-movements-ancient-map-view [group-id]
  (let [positions-in-time (group-positions-in-time history group-id)
        turns (sort (keys positions-in-time))
        positions (map #(get positions-in-time %) turns)
        minx (reduce (fn [acc pos] (min acc (:x pos))) (:x (first positions)) (rest positions))
        maxx (reduce (fn [acc pos] (max acc (:x pos))) (:x (first positions)) (rest positions))
        miny (reduce (fn [acc pos] (min acc (:y pos))) (:y (first positions)) (rest positions))
        maxy (reduce (fn [acc pos] (max acc (:y pos))) (:y (first positions)) (rest positions))
        radius 15
        x (max 0 (- minx radius))
        y (max 0 (- miny radius))
        end_x (min (width history) (+ maxx radius))
        end_y (min (height history) (+ maxy radius))
        w (- end_x x)
        h (- end_y y)
        pixels (map (fn [{ox :x, oy :y}] {:x (* map-scale-factor (- ox x)) :y (* map-scale-factor(- oy y))}) positions)
        colors (graded-colors (.size positions))
        colored-pixels (map #(assoc %1 :color %2) pixels colors)]
    (response-png-image
      (draw-colored-points
        (sub-image
          (ancient-map
            (world history))
          {:x (* map-scale-factor x) :y (* map-scale-factor y) :width (* map-scale-factor w) :height (* map-scale-factor h)})
        colored-pixels))))
