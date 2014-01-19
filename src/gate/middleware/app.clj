(ns gate.middleware.app
  (:require [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]))

(def api
  [wrap-params
   wrap-nested-params
   wrap-keyword-params])
