(defproject web-crawler "0.1.0-SNAPSHOT"
  :description "A web crawler"
  :url "http://lucasob.github.io"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clj-http "3.13.0"]
                 [lambdaisland/uri "1.19.155"]
                 [org.clj-commons/hickory "0.7.5"]
                 [cheshire "5.13.0"]
                 [org.apache.logging.log4j/log4j-api "2.20.0"]
                 [org.apache.logging.log4j/log4j-core "2.20.0"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.20.0"]]
  :plugins [[lein-cloverage "1.2.2"]]
  :main web-crawler.main
  :repl-options {:init-ns web-crawler.core}
  :profiles {:test {:dependencies [[org.testcontainers/testcontainers "1.20.1"]
                                   [org.wiremock.integrations.testcontainers/wiremock-testcontainers-module "1.0-alpha-14"]]}})
