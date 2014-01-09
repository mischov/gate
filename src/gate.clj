(ns gate
  (:require [gate.routes :refer [expand-routes default-dna]]
            [gate.router :refer [create-router]]))

(defmacro defroutes
  "Expands a sequence of routes at compile times."
  [name routes]
  `(def ~name (expand-routes ~routes)))

(defmacro defrouter
  "Expands a sequence of user-defined routes and creates
   a router from those routes.

   Equivalent to (def ... (create-router (expand-routes ...))),
   but expands routes at compile time. You want routes expanded
   at compile time, so if you wish to use create-router directly,
   use it with defroutes which will also expand routes at compile
   time."
  [name routes & opts]
  (let [options (if-let [o (first opts)] o {})
        dna (if-let [middleware (get options :middleware)]
              (assoc default-dna :middleware middleware)
              default-dna)]
    `(def ~name (create-router (expand-routes ~routes ~dna) ~options))))
