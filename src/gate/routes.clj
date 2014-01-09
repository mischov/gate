(ns gate.routes
  (:require [gate.route.path :as path]
            [gate.route.matcher :as matcher]
            [gate.route.handlers :as handlers]))

(def default-dna
  "DNA is information a route passes to child routes."
  {:path nil
   :path-parts []
   :path-params []
   :path-constraints {}
   :middleware []})

(defn ^:private update-dna-path
  "Combines path with parent-dna to create base dna for route."
  [parent-dna route]
  (let [p (get route :path)]
    (-> (path/parse-path p parent-dna)
        (update-in [:path] str p))))

(defn ^:private update-dna-middleware
  "Combines route and parent middleware then add the result to
   current-dna."
  [current-dna route]
  (let [m  (get route :middleware)
        pm (get current-dna :middleware)]
    (assoc current-dna :middleware (concat pm m))))

(defn ^:private create-route-dna
  "Creates dna for current route."
  [route parent-dna]
  (-> parent-dna
      (update-dna-path route)
      (update-dna-middleware route)))

(declare expand-routes)
;; expand-children defined in terms of expand-routes.

(defn ^:private expand-children
  "Expands a sequence of child routes."
  [route route-dna]
  (when-let [children (get route :children)]
    (expand-routes children route-dna)))

(defn expand-route
  "Expands a concise route into a sequence of expanded routes."
  ([route] (expand-route route default-dna))
  ([route parent-dna]
     (let [route-dna (create-route-dna route parent-dna)
           expanded-children (expand-children route route-dna)]
       (-> route
           (path/expand-path route-dna)
           (matcher/add-matcher)
           (handlers/expand-handlers)
           (concat expanded-children)))))

(defn expand-routes
  "Mapcats expand-route over a sequence of concise routes to
   create a flat sequence of full-routes."
  ([routes] (expand-routes routes default-dna))
  ([routes parent-dna]
       (mapcat #(expand-route % parent-dna) routes)))

