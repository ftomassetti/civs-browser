(ns civs-browser.views
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
    [civs-browser.basic :refer :all]
    [civs-browser.model :refer :all]
    )
  (:gen-class))

(import com.github.lands.draw.AncientMapDrawer)
(import java.io.ByteArrayOutputStream)
(import java.io.ByteArrayInputStream)
(import java.awt.RenderingHints)
(import javax.imageio.ImageIO)

; "Return a java.awt.BufferedImage"
;(def ancient-map
;  (memoize
;    (fn [world]
;      (AncientMapDrawer/drawAncientMap world))))

; Faster :D
(def ancient-map
  (memoize
    (fn [world] (javax.imageio.ImageIO/read (java.io.File. "examples-maps/ancient_map_seed_77.png")))))

(def map-scale-factor 4)

(defn image-bytes [image]
  (let [baos  (ByteArrayOutputStream.)
        _     (ImageIO/write image "png", baos )
        _     (.flush baos)
        bytes (.toByteArray baos)
        _     (.close baos)]
    bytes))

(defn response-png-image [image]
  {
    :status 200
    :headers {"Content-Type" "image/png"}
    :body (ByteArrayInputStream. (image-bytes image))
  })

(defn response-png-image-from-bytes [bytes]
  {
    :status 200
    :headers {"Content-Type" "image/png"}
    :body (ByteArrayInputStream. bytes)
    })

(defn world-ancient-map-view []
  (response-png-image (ancient-map (world history))))

(defn sub-image [^java.awt.image.BufferedImage image portview]
  (.getSubimage image (int (:x portview)) (int (:y portview)) (int (:width portview)) (int (:height portview))))

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

(defn draw-red-points [image points]
  (doall (for [{x :x y :y} points]
    (do
      (.setRGB image x y (.getRGB (java.awt.Color. 255 0 0)))
      (.setRGB image (dec x) y (.getRGB (java.awt.Color. 255 0 0)))
      (.setRGB image x (dec y) (.getRGB (java.awt.Color. 255 0 0)))
      (.setRGB image (inc x) y (.getRGB (java.awt.Color. 255 0 0)))
      (.setRGB image x (inc y) (.getRGB (java.awt.Color. 255 0 0))))))
  image)

(defn tibe-movements-ancient-map-view [group-id]
  (let [positions (vals (group-positions-in-time history group-id))
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
        pixels (map (fn [{ox :x, oy :y}] {:x (* map-scale-factor (- ox x)) :y (* map-scale-factor(- oy y))}) positions)]
  (response-png-image
    (draw-red-points
      (sub-image
        (ancient-map
          (world history))
        {:x (* map-scale-factor x) :y (* map-scale-factor y) :width (* map-scale-factor w) :height (* map-scale-factor h)})
      pixels))))

(defn view-layout [title & content]
  (html
    (doctype :xhtml-strict)
    (xhtml-tag "en"
      [:head
       [:meta {:http-equiv "Content-type"
               :content "text/html; charset=utf-8"}]
       [:title "Civs-Browser"]
       (include-css "/css/screen.css")]
        [:script {:src "/js/jquery-1.11.1.min.js"}]
      [:body
       [:h1 (str "Civs-Browser: " title)]
       [:ul.links
        [:li (link-to "/" "Homepage")]
        [:li (link-to "/tribes" "Groups")]]
       content
       [:script {:src "/js/cljs.js"}]])))

(defn tribes-homepage []
  (view-layout "Groups homepage"
    [:h2 "All the groups"]
    [:ul.groups
      (for [tribe-id (sort (groups-ids history))]
        [:li
         [:span (link-to (str "group/" tribe-id) (str "Group " tribe-id))]])]))

(defn plot-bytes [plot]
  (let [baos  (ByteArrayOutputStream.)
        _     (save plot baos)
        _     (.flush baos)
        bytes (.toByteArray baos)
        _     (.close baos)]
    bytes))

(defn world-pop-plot []
  (response-png-image-from-bytes
    (plot-bytes
      (line-chart (range 101) (popdata)))))

(defn homepage []
  (view-layout "Homepage"
    [:p (str "No. turns: " (n-turns history))]
    [:img.worldmap {:src "/ancient-map.png" }]
    [:h2 "World population over time"]
    (image "/worldpop.png")))

(defn- group-page-content [group-id]
  (let [ft (first-turn-for-group history group-id)
        lt (last-turn-for-group history group-id)]
    [:p (str "Alive from " ft " to " lt)]
    (image (str "/group/" group-id "/movements.png"))))

(defn group-page [group-id]
  (view-layout (str "Group "group-id)
    (if (exist-group? history group-id)
      (group-page-content group-id)
      [:p "This group does not exist"])))

(defn raw []
  (view-layout ("Raw view of the history file")
    [:p (str history)]))
