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

(defn found->navigable [host links]
  (->>
    links
    (reduce (fn [assembled found]
              (if-let [uri (and (seq found) (->uri host found))]
                (if (navigable-link? host uri)
                  (conj assembled uri)
                  assembled)
                assembled))
            [])
    (set)))

(defn crawl! [host]
  (let [urls-found (->
                     (str host)
                     (http/get {:throw-exceptions false})
                     (:body)
                     (hickory/parse)
                     (hickory/as-hickory)
                     (select-a-tags))]
    {:host  host
     :links (found->navigable host urls-found)}))

(defn crawler [visited-urls site-map {:keys [depth max-depth] :as cfg} uri]
  (when (< depth max-depth)
    (let [{:keys [links]} (crawl! uri)
          updated-visited (swap! visited-urls conj uri)
          _updated-site-map (swap! site-map assoc (str uri) (set (mapv str links)))
          new-to-visit (set/difference links updated-visited)
          next-cfg (assoc cfg :depth (inc depth))]
      (->> new-to-visit
           (map (fn [u] (future (crawler visited-urls site-map next-cfg u))))
           (map deref)
           (doall)))))

(defn create-crawler
  ([uri]
   (create-crawler {} uri))

  ([{:keys [max-depth] :or {max-depth 3}} uri]
   (let [visited-urls (atom #{})
         site-map (atom {})]
     (fn []
       (crawler visited-urls site-map {:depth 0 :max-depth max-depth} uri)
       @site-map))))
