(ns gate.routes.url
  (:require [clojure.string :as string])
  (:import [java.net URLEncoder]))

(defn ^:private name-encode
  "Calls `name` on part and encodes the result
   in UTF-8 for a query-string."
  [part]
  (URLEncoder/encode (name part) "UTF-8"))

(defn ^:private kv->string
  [[k v]]
  (str (name-encode k) "=" (name-encode v)))

(defn ^:private build-query-string
  [params path-parts]
  (when-let [query-params (not-empty (apply dissoc params path-parts))]
    (let [param-strings (map kv->string query-params)]
      (string/join "&" param-strings))))

(defn ^:private url-fn
  "Returns a function that constructs a url when supplied
   with params.

   If params are supplied that are not used to construct
   the url path, they are added as a query string."
  [{:keys [path-parts] :as route}]
  (fn [params]
    (let [query-string (build-query-string params path-parts)
          path (string/join "/" (map #(get params % %) path-parts))]
      (if query-string
        (str "/" path "?" query-string)
        (str "/" path)))))

(defn add-url-fn
  "Adds a url-constructing function to a route."
  [route]
  (assoc route :url-fn (url-fn route)))
