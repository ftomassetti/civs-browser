(ns civs-browser.views.core
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
    [potemkin :refer :all]
    [civs-browser.views.basic :refer :all]
    [civs-browser.views.group :refer :all])
  (:gen-class))

(import com.github.lands.draw.AncientMapDrawer)
(import java.io.ByteArrayOutputStream)
(import java.io.ByteArrayInputStream)
(import java.awt.image.BufferedImage)
(import java.awt.RenderingHints)
(import java.awt.Color)
(import javax.imageio.ImageIO)

;(import-vars
;  [civs-browser.views.group groups-view])

(defn exalt [n]
  (max (- (* 2.7 n) 1.72) 0.0))

(def prosperity-map
  (memoize
    (fn [world activity]
      (let [ w (-> world .getDimension .getWidth)
             h (-> world .getDimension .getHeight)
             img (BufferedImage. w h (BufferedImage/TYPE_INT_ARGB))
             g (.createGraphics img)]
        (doseq [y (range h)]
          (doseq [x (range w)]
            (let [pos {:x x :y y}]
              (when (isLand world pos)
                (let [p (base-prosperity-per-activity world pos activity)]
                  (.setColor g (Color. (- 255 (int (* (exalt p) 255.0))) (int (* (exalt p) 255.0)) 0)))
                (.fillRect g x y 1 1)))))
        (.dispose g)
        img))))

(defn world-ancient-map-view []
  (response-png-image (ancient-map (world history))))

(defn world-prosperity-map-view [activity]
  (response-png-image (prosperity-map (world history) activity)))

(defn scale-image [image factor]
  (let [ IMG_WIDTH  (int (* factor (.getWidth image)))
         IMG_HEIGHT (int (* factor (.getWidth image)))
         resizedImage (java.awt.image.BufferedImage. IMG_WIDTH, IMG_HEIGHT, (.getType image))
         g (.createGraphics resizedImage)
         _ (.drawImage g image, 0, 0, IMG_WIDTH, IMG_HEIGHT, nil)
         _ (.dispose g)
         _ (.setComposite g (java.awt.AlphaComposite/Src))
         _ (.setRenderingHint g (RenderingHints/KEY_INTERPOLATION) (RenderingHints/VALUE_INTERPOLATION_BILINEAR))
         _ (.setRenderingHint g (RenderingHints/KEY_RENDERING)    (RenderingHints/VALUE_RENDER_QUALITY))
         _ (.setRenderingHint g (RenderingHints/KEY_ANTIALIASING) (RenderingHints/VALUE_ANTIALIAS_ON)) ]
    resizedImage))

(defn world-pop-plot []
  (response-png-image-from-bytes
    (plot-bytes
      (line-chart (range 101) (popdata)))))

(defn- game-state-page-content [turn]
  [:p "A Fantastic turn!"])

(defn game-state-page [turn]
  (view-layout (str "World at " turn)
    (if (exist-turn? history turn)
      (game-state-page-content turn)
      [:p "This turn does not exist"])))

(defn- game-state-map-image [game turn]
  (let [ world (.world game)
         w (-> world .getDimension .getWidth)
         h (-> world .getDimension .getHeight)
         img (base-image world)
         g (.createGraphics img)]
    (doseq [group (groups-alive game)]
      (let [pos (group-position-at history turn (.id group))]
        (let [_ 1]
          (.setColor g (Color. 255 0 255))
          (.fillRect g (:x pos) (:y pos) 1 1))))
    (.dispose g)
    img))

(defn game-state-map [turn]
  (let [g (get (:game-snapshots history) turn)]
    (response-png-image (game-state-map-image g turn))))

(defn homepage []
  (view-layout "Homepage"
    [:p (str "No. turns: " (n-turns history))]
    [:img.worldmap {:src "/ancient-map.png" }]
    [:h2 "World population over time"]
    (image "/worldpop.png")))
