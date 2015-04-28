(ns gate
  (:require [potemkin :refer [import-fn import-macro]]
            [clojure.tools.macro :refer [name-with-attributes]]
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
   time.

   Accepts docstrings."
  [name & args]
  
  (let [[name [routes & [router-settings]]] (name-with-attributes name args)]

    `(let [dna# (init-dna ~router-settings)]
     (def ~name
       (create-router (expand-routes ~routes dna#) ~router-settings)))))


(defmacro handler
  "Convincience macro for constructing anonymous Gate
   handlers with easy access to request params.

   Using a vector of symbols for req-bindings binds names from
   the :params key of a ring request map, though the whole request
   can be bound by adding ':as' to req-bindings vector and following
   it with whatever symbol you want the request bound as.

   Using a symbol for req-bindings binds the whole request to
   that symbol."
  [req-bindings & body]

  (create-handler req-bindings body))


(defmacro defhandler
  "Convinience macro for defining named Gate handlers.

   Accepts docstrings."
  [name & args]
  
  (let [[name [req-bindings & body]] (name-with-attributes name args)]
    
    `(def ~name
     ~(create-handler req-bindings body))))
