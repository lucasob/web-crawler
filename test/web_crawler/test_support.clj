(ns web-crawler.test-support
  (:require [clojure.test :refer :all]))

(defn html-with-single-link [to]
  (format
    "<!DOCTYPE html>
    <html>
      <head>
        <title>Sample</title
      </head>
      <body>
        <a href=\"%s\">Can you find me?</a>
      </body>
    </html>"
    to))

(def html-with-no-links
  "<!DOCTYPE html>
  <html>
    <head>
      <title>I am empty</title
    </head
    <body>
      No links here
    </body
  </html>")
