(ns gate.middleware
  (:require [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [gate.route.handlers :as handlers]))

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
