(ns gate.routes.dna
  (:require [gate.routes.path :refer [parse-path]]))

(def blank-dna
  "DNA is information a route passes to child routes."
  {:path nil
   :path-parts []
   :path-params []
   :middleware []})

(defn ^:private update-dna-path
  "Combines path with parent-dna to create base dna for route."
  [parent-dna route]
  (let [p (get route :path)]
    (parse-path p parent-dna)))

(defn ^:private update-dna-middleware
  "Combines route and parent middleware then add the result to
   current-dna."
  [current-dna route]
  (let [m  (get route :middleware)
        pm (get current-dna :middleware)]
    (assoc current-dna :middleware (concat pm m))))

(defn create-route-dna
  "Creates dna for current route."
  [route parent-dna]
  (-> parent-dna
      (update-dna-path route)
      (update-dna-middleware route)))

(defn init-dna
  [base-dna router-settings]
  (if-let [middleware (get router-settings :middleware)]
    (assoc base-dna :middleware middleware)
    base-dna))
