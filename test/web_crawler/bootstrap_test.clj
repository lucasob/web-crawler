(ns web-crawler.bootstrap-test
  (:require [clojure.test :refer :all]
            [lambdaisland.uri :as uri]
            [web-crawler.fixtures :as fixtures]
            [web-crawler.main :as main]
            [web-crawler.test-support :as support]
            [web-crawler.wiremock :as wiremock]))

(use-fixtures :once fixtures/with-wiremock)
(use-fixtures :each fixtures/reset-wiremock)

(deftest bootstrap-success-from-main
  (testing "entry-point validation"
    (wiremock/stub-for!
      [{:request  {:method "GET" :urlPath "/parent"}
        :response {:status  200
                   :headers {"Content-Type" "text/html"}
                   :body    (support/html-with-single-link "/child")}}])
    (let [parent-link (uri/parse (wiremock/url "parent"))
          child-link (-> (into {} parent-link) (merge {:path "/child"}) (uri/map->URI) (str))]
      (is (= {(str parent-link) #{child-link}}
             (main/crawl-url! (wiremock/url "parent") 1))))))