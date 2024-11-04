(ns web-crawler.main
  (:require
    [lambdaisland.uri :as uri]
    [web-crawler.core :as crawler]
    [cheshire.core :as cheshire]))

(defn -main [& args]
  (-> args (first) (uri/parse) (crawler/crawl-all!) (cheshire/generate-string) (println)))
