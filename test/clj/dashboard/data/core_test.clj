(ns clj.dashboard.data.core-test
  (:require [dashboard.data.core :as sut]
            [clojure.edn :as edn]
            [clojure.test :refer [deftest testing is]]))

(testing "data processing library"
  (deftest full-join-nil-strategy-matching-length
    (let [tupel1 [[1 2 3 4] [2 3 1 2]]
          tupel2 [[1 2 5 6] [2 1 0 0]]
          expected [[1 2 3 4 5 6] [2 3 1 2 nil nil] [2 1 nil nil 0 0]]]
      (is (= expected (sut/full-join-nil tupel1 tupel2))))))