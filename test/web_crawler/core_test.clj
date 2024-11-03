(ns web-crawler.core-test
  (:require
    [clojure.test :refer :all]
    [lambdaisland.uri :as uri]
    [web-crawler.core :as crawler]
    [web-crawler.fixtures :as fixtures]
    [web-crawler.wiremock :as wiremock]))

(use-fixtures :once fixtures/with-wiremock)

(def html-with-single-link
  "<!DOCTYPE html>
  <html>
    <head>
      <title>Sample</title
    </head
    <body>
      <a href=\"/not-bananas\">Can you find me?</a>
    </body
  </html>")

(def html-with-no-links
  "<!DOCTYPE html>
  <html>
    <head>
      <title>I am empty</title
    </head
    <body>
      No links here
    </body
  </html>")

(deftest get-links
  (testing "Can return a single url on a page"
    (wiremock/stub-for!
      [{:request  {:method "GET" :urlPath "/bananas"}
        :response {:status  200
                   :headers {"Content-Type" "text/html"}
                   :body    html-with-single-link}}])
    (let [banana-link (uri/parse (wiremock/url "bananas"))
          expected-link (-> (into {} banana-link) (merge {:path "/not-bananas"}) (uri/map->URI))
          response (-> (wiremock/url "bananas") (uri/parse) (crawler/crawl!))]
      (is (= {:host banana-link :links [expected-link]} response)))))

(deftest follow-links
  (testing "Can follow a single, relative url on the page"
    (wiremock/stub-for!
      [{:request  {:method "GET" :urlPath "/bananas"}
        :response {:status  200
                   :headers {"Content-Type" "text/html"}
                   :body    html-with-single-link}}
       {:request  {:method "GET" :urlPath "/not-bananas"}
        :response {:status  200
                   :headers {"Content-Type" "text/html"}
                   :body    html-with-no-links}}])
    (let [banana-link (uri/parse (wiremock/url "bananas"))
          not-banana-link (uri/parse (wiremock/url "not-bananas"))]
      (is (= {banana-link     #{not-banana-link}
              not-banana-link #{}}
             (-> (wiremock/url "bananas") (uri/parse) (crawler/do-the-rawr)))))))
