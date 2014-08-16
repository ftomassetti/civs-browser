(ns civs-browser.model-test
  (:require [clojure.test :refer :all]
            [civs-browser.core :refer :all]
            [civs-browser.model :refer :all]))

(def my-history (load-history-file-fressian "examples-history/w77_100turns.history" "examples-worlds"))

(deftest test-n-turns
  (is (= 100 (n-turns my-history))))

;(deftest test-tribes-ids
;  (let [res (tribes-ids my-history)])