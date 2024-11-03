(ns web-crawler.core
  (:require
    [clj-http.client :as http]
    [clojure.core.async :as async]
    [clojure.set :as set]
    [hickory.core :as hickory]
    [hickory.select :as select]
    [lambdaisland.uri :as uri]))

(defn select-a-tags [hck]
  (->> hck (select/select (select/tag :a)) (mapv (comp :href :attrs))))

(defn default-fields [known other]
  (let [{:keys [scheme host]
         :or   {scheme (.scheme known)
                host   (.host known)}} other]
    (uri/map->URI
      (merge
        other
        {:scheme (or (:scheme other) (.scheme known))
         :host   (or (:host other) (.host known))
         :port   (or (:port other) (.port known))}))))


(defn crawl! [uri]
  (let [urls-found (->
                     (str uri)
                     (http/get {:throw-exceptions false})
                     (:body)
                     (hickory/parse)
                     (hickory/as-hickory)
                     (select-a-tags))]
    {:host uri
     :links (->>
              urls-found
              (mapv
                (fn [ref] (->> ref (uri/parse) (default-fields uri))))
              (set))}))


(defn crawler [visited-urls site-map uri]
  (let [{:keys [links]} (crawl! uri)
        updated-visited (swap! visited-urls conj uri)
        _updated-site-map (swap! site-map assoc uri links)
        new-to-visit (set/difference links updated-visited)]
    (->> new-to-visit
         (map (fn [u] (future (crawler visited-urls site-map u))))
         (map deref)
         (doall))))

(defn do-the-rawr [uri]
  (let [visited-urls (atom #{})
        site-map (atom {})
        _blocking (crawler visited-urls site-map uri)]
    @site-map))
