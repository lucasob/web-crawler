(ns web-crawler.links-test
  (:require [clojure.test :refer :all]
            [lambdaisland.uri :as uri]
            [web-crawler.core :as crawler]))

(deftest correctly-selects-a-navigable-link
  (let [root (uri/parse "https://www.google.com")
        ok-for? (partial crawler/navigable-link? root)]
    (testing "A standard route"
      (is (true? (ok-for? (uri/parse "https://www.google.com/redirect")))))
    (testing "On subdomain rejected"
      (is (false? (ok-for? (uri/parse "https://www.images.google.com")))))
    (testing "Mailto is rejected"
      (is (false? (ok-for? (uri/parse "mailto:demo@this.com")))))
    (testing "relative fragment is rejected"
      (is (false?
            (ok-for?
              (-> (into {} root) (assoc :fragment "fragmentable") (uri/map->URI))))))))

(deftest correctly-generates-a-set-of-navigable-links
  (let [host (uri/parse "https://some.domain")
        found [nil "" "/relative" "#onTheSamePage" "https://sub.some.domain" "https://something.else" "javascript:void(0)"]]
    (is (= #{(uri/parse "https://some.domain/relative")}
           (crawler/found->navigable host found)))))
