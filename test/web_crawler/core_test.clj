(ns web-crawler.core-test
  (:require
    [clojure.test :refer :all]
    [web-crawler.fixtures :as fixtures]
    [web-crawler.wiremock :as wiremock]))

(use-fixtures :once fixtures/with-wiremock)

(deftest a-test
  (testing "bootstrap"
    (is (some? (re-matches #"http:\/\/localhost:[\d]+\/bananas" (wiremock/url "bananas"))))))
