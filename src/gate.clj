(ns gate
  (:require [gate.routes.dna :refer [init-dna]]
            [gate.routes :refer [expand-routes]]
            [gate.router :refer [create-router]]))

(defmacro defrouter
  "Expands a sequence of user-defined routes and creates
   a router from those routes.

   Equivalent to (def ... (create-router (expand-routes ...))),
   but expands routes at compile time.

   For performance reasons, you want routes expanded at compile
   time."
  [name routes & [router-settings]]
  `(let [dna# (init-dna ~router-settings)]
     (def ~name
       (create-router (expand-routes ~routes dna#) ~router-settings))))
