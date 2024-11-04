(ns web-crawler.main
  (:require
    [lambdaisland.uri :as uri]
    [web-crawler.core :as crawler]
    [cheshire.core :as cheshire]))

(defn crawl-url! [url max-depth]
  (let [do-crawl (->> url (uri/parse) (crawler/create {:max-depth max-depth}))]
    (do-crawl)))

(defn -main [& [uri max-depth]]
  (let [depth (try (Integer/parseInt max-depth) (catch Exception _e 1))]
    (->
      (crawl-url! uri depth)
      (cheshire/generate-string)
      (println))
    (System/exit 0)))