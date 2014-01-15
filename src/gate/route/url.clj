(ns gate.route.url
  (:require [clojure.string :as string]))

(defn url-fn
  [{:keys [path-parts] :as route}]
  (fn [params]
    (str "/"
         (string/join "/" (map #(get params % %) path-parts)))))

(defn add-url-fn
  [route]
  (assoc route :url-fn (url-fn route)))
