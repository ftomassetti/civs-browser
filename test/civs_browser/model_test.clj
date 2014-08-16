(ns civs-browser.model-test
  (:require [clojure.test :refer :all]
            [civs-browser.core :refer :all]
            [civs-browser.model :refer :all]))

(def my-history (load-history-file-fressian "examples-history/w77_100turns.history" "examples-worlds"))

(deftest test-n-turns
  (is (= 100 (n-turns my-history))))

(deftest test-exist-tribe?
  (is (= true (exist-tribe? my-history 1)))
  (is (= false (exist-tribe? my-history 123456))))