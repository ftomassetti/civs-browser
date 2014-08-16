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
    [clojure.edn :as edn])
  (:gen-class))

(defn n-turns [history]
  (.size (keys (:facts history))))