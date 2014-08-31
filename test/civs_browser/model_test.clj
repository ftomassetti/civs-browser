(ns civs-browser.model-test
  (:require [clojure.test :refer :all]
            [civs.model.history :refer :all]
            [civs-browser.core :refer :all]
            [civs-browser.model :refer :all]))

(def my-history (load-history-file-fressian "examples-history/w77_100turns.history" "examples-worlds"))

;#########################################
; Turns
;#########################################

; TODO move these tests in Civs

(deftest test-n-turns
  (is (= 100 (n-turns my-history))))

(deftest test-turns
  (is (= (range 101) (turns my-history))))

(deftest test-exist-turn
  (is (= true (exist-turn? my-history 0)))
  (is (= true (exist-turn? my-history 100)))
  (is (= true (exist-turn? my-history 50)))
  (is (= false (exist-turn? my-history -1)))
  (is (= false (exist-turn? my-history 101))))

;#########################################
; Game
;#########################################

(deftest test-exist-group?
  (is (= true (exist-group? my-history 1)))
  (is (= false (exist-group? my-history 123456))))

;#########################################
; World
;#########################################

;#########################################
; Groups
;#########################################