(ns web-crawler.fixtures
  (:require [web-crawler.wiremock :as wiremock]))

(defn with-wiremock [f]
  (wiremock/start!)
  (f))

(defn reset-wiremock [f]
  (f)
  (wiremock/refresh!))
