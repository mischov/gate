(ns gate.routes
  (:require [gate.routes.dna :refer [create-route-dna blank-dna]]
            [gate.routes.handler :refer [expand-handlers]]))


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
  ([route] (expand-route route blank-dna))
  
  ([route base-dna]
     
       (when (valid? route)
         (let [route-dna (create-route-dna route base-dna)
               expanded-children (expand-children route route-dna)]
           
           (-> route
               (merge route-dna)
               (expand-handlers)
               (concat expanded-children))))))


(defn expand-routes
  "Mapcats expand-route over a sequence of concise routes to
   create a flat sequence of expanded routes."
  ([routes] (expand-routes routes blank-dna))
  
  ([routes base-dna]
     
       (mapcat #(expand-route % base-dna) routes)))

