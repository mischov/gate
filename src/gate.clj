(ns gate
  (:require [potemkin :refer [import-fn import-macro]]
            [gate.routes.dna]
            [gate.routes]
            [gate.router]
            [gate.handler]))


(import-fn gate.routes.dna/init-dna)
(import-fn gate.routes/expand-routes)
(import-fn gate.router/create-router)
(import-fn gate.handler/create-handler)


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


(defmacro defhandler
  "Convinience function for constructing Gate handlers.

   Using a vector of symbols for req-bindings binds names from
   the :params key of a ring request map, though the whole request
   can be bound by adding ':as' to req-bindings vector and following
   it with whatever symbol you want the request bound as.

   Using a symbol for req-bindings binds the whole request to
   that symbol."
  [name req-bindings & body]
  
  `(def ~name
     ~(create-handler req-bindings body)))
