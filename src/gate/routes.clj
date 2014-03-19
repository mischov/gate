(ns gate.routes
  (:require [gate.routes.dna :refer [create-route-dna]]
            [gate.routes.url :refer [add-url-fn]]
            [gate.routes.handler :refer [expand-handlers]]))

(def default-dna
  "DNA is information a route passes to child routes."
  {:path nil
   :path-parts []
   :path-params []
   :middleware []})

;; expand-children defined in terms of expand-routes.
(declare expand-routes)

(defn ^:private expand-children
  "Expands a sequence of child routes."
  [route route-dna]
  (when-let [children (get route :children)]
    (expand-routes children route-dna)))

(defn valid?
  "Returns true if route is valid, otherwise throws."
  [route]
  (let [name (get route :name)
        path (get route :path)]
    (cond
     (nil? name)
       (throw (ex-info (str "Routes require a :name key, but this key is missing from: " route) {}))
     (nil? path)
       (throw (ex-info (str "Routes require a :path key, but this key is missing from: " route) {}))
     :else true)))

(defn expand-route
  "Expands a concise route into a sequence of expanded routes."
  ([route] (expand-route route default-dna))
  ([route parent-dna]
       (when (valid? route)
         (let [route-dna (create-route-dna route parent-dna)
               expanded-children (expand-children route route-dna)]
           (-> route
               (merge route-dna)
               (add-url-fn)
               (expand-handlers)
               (concat expanded-children))))))

(defn expand-routes
  "Mapcats expand-route over a sequence of concise routes to
   create a flat sequence of full-routes."
  ([routes] (expand-routes routes default-dna))
  ([routes parent-dna]
       (mapcat #(expand-route % parent-dna) routes)))

