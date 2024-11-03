(ns web-crawler.core
  (:require
    [clj-http.client :as http]
    [clojure.set :as set]
    [hickory.core :as hickory]
    [hickory.select :as select]
    [lambdaisland.uri :as uri]))

(defn select-a-tags [hck]
  (->> hck (select/select (select/tag :a)) (mapv (comp :href :attrs))))

(defn with-defaults [known other]
  (let [{:keys [scheme host]
         :or   {scheme (.scheme known)
                host   (.host known)}} other]
    (uri/map->URI
      (merge
        other
        {:scheme (or (:scheme other) (.scheme known))
         :host   (or (:host other) (.host known))
         :port   (or (:port other) (.port known))}))))

(defn is-relative-fragment? [known {:keys [fragment] :as other}]
  (boolean
    (and (some? fragment) (dissoc known :fragment) (dissoc other :fragment))))

(defn ->uri [host-uri uri-like]
  (->> uri-like (uri/parse) (with-defaults host-uri)))

(def ^:const SCHEME_HTTP "http")
(def ^:const SCHEME_HTTPS "https")

(defn valid-scheme? [scheme]
  (#{SCHEME_HTTP SCHEME_HTTPS} scheme))

(defn navigable-link?
  [root to-inspect]
  (boolean
    (and
      (= (:host root) (:host to-inspect))
      (valid-scheme? (:scheme to-inspect))
      (not (is-relative-fragment? root to-inspect)))))

(defn crawl! [host]
  (let [urls-found (->
                     (str host)
                     (http/get {:throw-exceptions false})
                     (:body)
                     (hickory/parse)
                     (hickory/as-hickory)
                     (select-a-tags))]
    {:host host
     :links (->>
              urls-found
              (reduce (fn [assembled found]
                        (when-let [uri (and (some? found) (->uri host found))]
                          (when (navigable-link? host uri)
                            (conj assembled uri))))
                      [])
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
