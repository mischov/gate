(ns gate
  (:require [gate.routes :as routes]
            [gate.router :as router]
            [gate.response :as response]))

(def expand-routes routes/expand-routes)
(def create-router router/create-router)

(defn prepare-dna
  [default-dna settings]
  (if-let [middleware (get settings :middleware)]
    (assoc default-dna :middleware middleware)
    default-dna))

(defmacro defroutes
  "Expands a sequence of routes at compile times."
  [name routes & [settings]]
  (let [dna (prepare-dna routes/default-dna settings)]
    `(def ~name (expand-routes ~routes ~dna))))

(defmacro defrouter
  "Expands a sequence of user-defined routes and creates
   a router from those routes.

   Equivalent to (def ... (create-router (expand-routes ...))),
   but expands routes at compile time. You want routes expanded
   at compile time, so if you wish to use create-router directly,
   use it with defroutes which will also expand routes at compile
   time."
  [name routes & [settings]]
  (let [dna (prepare-dna routes/default-dna settings)]
    `(def ~name
       (create-router (expand-routes ~routes ~dna) ~settings))))
