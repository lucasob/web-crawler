(defproject web-crawler "0.1.0-SNAPSHOT"
  :description "A web crawler"
  :url "http://lucasob.github.io"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :repl-options {:init-ns web-crawler.core}
  :profiles {:test {:dependencies [[org.testcontainers/testcontainers "1.20.1"]
                                   [org.wiremock.integrations.testcontainers/wiremock-testcontainers-module "1.0-alpha-14"]]}})
