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

(defn group-pop-plot [group-id]
  (let [data (group-popdata-in-time history group-id)]
    (response-png-image-from-bytes
      (plot-bytes
        (line-chart (turns-in-which-group-is-alive history group-id) data)))))

(defn- group-page-content [group-id]
  (let [ft (first-turn-for-group history group-id)
        lt (last-turn-for-group history group-id)
        ts (sort (turns-in-which-group-is-alive history group-id))]
    (list
      [:p (str "Alive from " ft " to " lt)]
      [:h2 "Migrations"]
      [:img.movements {:src (str "/group/" group-id "/movements.png")}]
      [:h2 "Population"]
      [:img.population.plot {:src (str "/group/" group-id "/pop.png")}]
      [:h2 "Events"]
      (for [t ts]
        (list
          [:h3 (str "Turn " t)]
          [:ul (for [f (facts-by-turn-and-group history t group-id)]
            [:li (str f)])
          ]))
     )))

(defn- label [group-id]
  (let [name (group-name history group-id)
        label (if (= :unnamed name) (str "Group " group-id) name)]
    label))

(defn group-view [group-id]
  (view-layout (label group-id)
    (if (exist-group? history group-id)
      (group-page-content group-id)
      [:p "This group does not exist"])))

(defn- li-per-group [group-id]
  (try
    [:li
      [:span (link-to (str "group/" group-id) (label group-id)
          " (" (str (first-turn-for-group history group-id)) "-" (str (last-turn-for-group history group-id)) ")")]]
    (catch Exception e (throw (Exception. (str "Working on " group-id) e)))))

(defn groups-view []
  (let [all-groups (sort (groups-ids history))
        [groups-a groups-bc] (split-at (/ (.size all-groups) 3) all-groups)
        [groups-b groups-c]  (split-at (/ (.size all-groups) 3) groups-bc)]
    (view-layout "Groups homepage"
      [:h2 "All the groups"]
      [:ul.groups
       (for [tribe-id groups-a]
         (li-per-group tribe-id))]
      [:ul.groups
       (for [tribe-id groups-b]
         (li-per-group tribe-id))]
      [:ul.groups
       (for [tribe-id groups-c]
         (li-per-group tribe-id))])))

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
