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
  "returns true if given the root url, we are expected to follow this link later."
  [root to-inspect]
  (boolean
    (and
      (= (:host root) (:host to-inspect))
      (valid-scheme? (:scheme to-inspect))
      (not (is-relative-fragment? root to-inspect)))))

(defn found->navigable
  "Returns a set of links, from a host, that should be traversed next."
  [host links]
  (->>
    links
    (reduce (fn [assembled found]
              (or
                (if-let [uri (and (seq found) (->uri host found))]
                  (when (navigable-link? host uri)
                    (conj assembled uri)))
                assembled))
            [])
    (set)))

(defn scrape! [host]
  (let [urls-found (->
                     (str host)
                     (http/get {:throw-exceptions false})
                     (:body)
                     (hickory/parse)
                     (hickory/as-hickory)
                     (select-a-tags))]
    {:host  host
     :links (found->navigable host urls-found)}))

(defn crawl! [visited-urls site-map {:keys [depth max-depth] :as cfg} uri]
  (when (< depth max-depth)
    (let [{:keys [links]} (scrape! uri)
          updated-visited (swap! visited-urls conj uri)
          _updated-site-map (swap! site-map assoc uri links)
          new-to-visit (set/difference links updated-visited)
          next-cfg (assoc cfg :depth (inc depth))]
      (->> new-to-visit
           (map (fn [u] (future (crawl! visited-urls site-map next-cfg u))))
           (map deref)
           (doall)))))

(defn create
  "`create` returns a function that when invoked, returns the sitemap from `uri`,
  which is obtained by crawling the specified uri to the configured parameters."
  ([uri] (create {} uri))

  ([{:keys [max-depth] :or {max-depth 3}} uri]
   (let [visited-urls (atom #{})
         site-map (atom {})]
     (fn []
       (crawl! visited-urls site-map {:depth 0 :max-depth max-depth} uri)
       (into {} (map (fn [[site urls]] {(str site) (->> urls (mapv str) (set))}) @site-map))))))
