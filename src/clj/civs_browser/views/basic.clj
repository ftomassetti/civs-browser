(ns civs-browser.views.basic
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
    [civs-browser.model :refer :all])
  (:gen-class))

(import com.github.lands.draw.AncientMapDrawer)
(import java.io.ByteArrayOutputStream)
(import java.io.ByteArrayInputStream)
(import java.awt.image.BufferedImage)
(import java.awt.RenderingHints)
(import java.awt.Color)
(import javax.imageio.ImageIO)

(def map-scale-factor 4)

(defn image-bytes [image format]
  (let [baos  (ByteArrayOutputStream.)
        _     (ImageIO/write image format, baos )
        _     (.flush baos)
        bytes (.toByteArray baos)
        _     (.close baos)]
    bytes))

(defn response-png-image-from-bytes [bytes]
  {
    :status 200
    :headers {"Content-Type" "image/png"}
    :body (ByteArrayInputStream. bytes)
  })

(defn response-gif-image-from-bytes [bytes]
  {
    :status 200
    :headers {"Content-Type" "image/gif"}
    :body (ByteArrayInputStream. bytes)
  })

(defn response-png-image [image]
  (response-gif-image-from-bytes (image-bytes image "png")))

(defn response-gif-image [image]
  (response-gif-image-from-bytes (image-bytes image "gif")))

(defn base-image
  "Draw a base image with blue ocean and white land"
  [world]
  (let [ w (-> world .getDimension .getWidth)
         h (-> world .getDimension .getHeight)
         img (BufferedImage. w h (BufferedImage/TYPE_INT_ARGB))
         g (.createGraphics img)]
    (doseq [y (range h)]
      (doseq [x (range w)]
        (let [pos {:x x :y y}]
          (if (isLand world pos)
            (let [_ 1]
              (.setColor g (Color. 255 255 255))
              (.fillRect g x y 1 1))
            (do
              (.setColor g (Color. 0 0 255))
              (.fillRect g x y 1 1))))))
    (.dispose g)
    img))

(defn plot-bytes [plot]
  (let [baos  (ByteArrayOutputStream.)
        _     (save plot baos)
        _     (.flush baos)
        bytes (.toByteArray baos)
        _     (.close baos)]
    bytes))

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

(defn draw-colored-points [image colored-points]
  (doall (for [{x :x y :y color :color} colored-points]
           (do
             (.setRGB image x y (.getRGB color))
             (.setRGB image (dec x) y (.getRGB color))
             (.setRGB image x (dec y) (.getRGB color))
             (.setRGB image (inc x) y (.getRGB color))
             (.setRGB image x (inc y) (.getRGB color)))))
  image)

(defn sub-image [^java.awt.image.BufferedImage image portview]
  (.getSubimage image (int (:x portview)) (int (:y portview)) (int (:width portview)) (int (:height portview))))

; "Return a java.awt.BufferedImage"
;(def ancient-map
;  (memoize
;    (fn [world]
;      (AncientMapDrawer/drawAncientMap world))))

; Faster :D
(def ancient-map
  (memoize
    (fn [world] (javax.imageio.ImageIO/read (java.io.File. "examples-maps/ancient_map_seed_77.png")))))