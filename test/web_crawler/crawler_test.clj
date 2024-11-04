(ns web-crawler.crawler_test
  (:require
    [clojure.test :refer :all]
    [lambdaisland.uri :as uri]
    [web-crawler.core :as crawler]
    [web-crawler.fixtures :as fixtures]
    [web-crawler.wiremock :as wiremock]
    [web-crawler.test-support :as support]))

(use-fixtures :once fixtures/with-wiremock)
(use-fixtures :each fixtures/reset-wiremock)

(deftest get-links
  (testing "Can return a single url on a page"
    (wiremock/stub-for!
      [{:request  {:method "GET" :urlPath "/parent"}
        :response {:status  200
                   :headers {"Content-Type" "text/html"}
                   :body    (support/html-with-single-link "/child")}}])
    (let [parent-link (uri/parse (wiremock/url "parent"))
          expected-link (-> (into {} parent-link) (merge {:path "/child"}) (uri/map->URI))
          response (-> (wiremock/url "parent") (uri/parse) (crawler/scrape!))]
      (is (= {:host parent-link :links #{expected-link}} response)))))

(deftest getting-links-ignores-anchors
  (testing "Gracefully skips anchor links"
    (wiremock/stub-for!
      [{:request  {:method "GET" :urlPath "/parent"}
        :response {:status  200
                   :headers {"Content-Type" "text/html"}
                   :body    (support/html-with-single-link "#ItsBigBrainTime")}}])
    (let [parent-link (uri/parse (wiremock/url "parent"))]
      (is (= {:host parent-link :links #{}} (-> (wiremock/url "parent") (uri/parse) (crawler/scrape!)))))))

(deftest follow-links
  (testing "Can follow a single, relative url on the page"
    (wiremock/stub-for!
      [{:request  {:method "GET" :urlPath "/parent"}
        :response {:status  200
                   :headers {"Content-Type" "text/html"}
                   :body    (support/html-with-single-link "/child")}}
       {:request  {:method "GET" :urlPath "/child"}
        :response {:status  200
                   :headers {"Content-Type" "text/html"}
                   :body    support/html-with-no-links}}])
    (let [do-crawl (-> (wiremock/url "parent") (uri/parse) (crawler/create))
          parent-link (-> (wiremock/url "parent") (uri/parse) (str))
          child-link (-> (wiremock/url "child") (uri/parse) (str))]
      (is (= {parent-link #{child-link} child-link #{}} (do-crawl))))))

(deftest no-cycles
  (testing "Given pages will always link back some way, ensure we do not chase infinitely"
    (wiremock/stub-for!
      [{:request  {:method "GET" :urlPath "/parent"}
        :response {:status  200
                   :headers {"Content-Type" "text/html"}
                   :body    (support/html-with-single-link "/child")}}
       {:request  {:method "GET" :urlPath "/child"}
        :response {:status  200
                   :headers {"Content-Type" "text/html"}
                   :body    (support/html-with-single-link "/parent")}}])
    (let [do-crawl (-> (wiremock/url "parent") (uri/parse) (crawler/create))
          parent-link (-> (wiremock/url "parent") (uri/parse) (str))
          child-link (-> (wiremock/url "child") (uri/parse) (str))]
      (is (= {parent-link #{child-link} child-link #{parent-link}} (do-crawl))))))

(deftest respects-depth
  (testing "Respects the depth"
    (wiremock/stub-for!
      [{:request  {:method "GET" :urlPath "/parent"}
        :response {:status  200
                   :headers {"Content-Type" "text/html"}
                   :body    (support/html-with-single-link "/child")}}
       {:request  {:method "GET" :urlPath "/child"}
        :response {:status  200
                   :headers {"Content-Type" "text/html"}
                   :body    (support/html-with-single-link "/second-child")}}
       {:request  {:method "GET" :urlPath "/second-child"}
        :response {:status  200
                   :headers {"Content-Type" "text/html"}
                   :body    support/html-with-no-links}}])
    (let [do-crawl (->> (wiremock/url "parent") (uri/parse) (crawler/create {:max-depth 2}))
          parent-link (-> (wiremock/url "parent") (uri/parse) (str))
          child-link (-> (wiremock/url "child") (uri/parse) (str))
          second-child-link (-> (wiremock/url "second-child") (uri/parse) (str))]
      (is (= {parent-link #{child-link} child-link #{second-child-link}} (do-crawl))))))
