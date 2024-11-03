(ns web-crawler.wiremock
  (:import (org.testcontainers.utility DockerImageName)
           (org.wiremock.integrations.testcontainers WireMockContainer)))

(defonce wiremock-container
         (->
           (DockerImageName/parse "wiremock/wiremock:3.1.0")
           (WireMockContainer.)
           (.withReuse true)))

(defn start! []
  (.start wiremock-container))

(defn stop! []
  (.stop wiremock-container))

(defn is-running? []
  (some? (.getContainerId wiremock-container)))

(defn url
  ([] (url ""))
  ([path] (.getUrl wiremock-container path)))
