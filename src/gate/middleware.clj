(ns gate.middleware
  (:require [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]))

(defn ^:private uniquely-add
  [middleware middlewares]
  (if (some #{middleware} middlewares)
    middlewares
    (conj middlewares middleware)))

(defn ^:private dedup-middlewares
  [middlewares]
  (loop [m (first middlewares)
         ms (next middlewares)
         result []]
    (if-not ms
      (uniquely-add m result)
      (recur (first ms) (next ms) (uniquely-add m result)))))

(defn wrap-handler
  [handler middlewares]
  (let [middlewares (dedup-middlewares middlewares)
        wrapper (apply comp middlewares)]
    (if wrapper
      (wrapper handler)
      handler)))

(defn combine
  "Combines middlewares. Middleware will be called left to right."
  [& middlewares]
  (apply comp middlewares))

(defmacro defcomp
  "Defines a composite middleware made from one or more other
   middlewares."
  [name & middlewares]
  `(def ~name (combine ~@middlewares)))

(defcomp api
  wrap-params
  wrap-nested-params
  wrap-keyword-params)
