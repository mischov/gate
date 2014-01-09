(ns gate
  (:require [gate.routes :as routes]
            [gate.router :as router]))

(def expand-routes routes/expand-routes)
(def create-router router/create-router)

(defn get-settings
  [opts]
  (if-let [settings (first opts)] settings {}))

(defn prepare-dna
  [default-dna settings]
  (if-let [middleware (get settings :middleware)]
    (assoc default-dna :middleware middleware)
    default-dna))

(defmacro defroutes
  "Expands a sequence of routes at compile times."
  [name routes & opts]
  (let [settings (get-settings opts)
        dna (prepare-dna routes/default-dna settings)]
    `(def ~name (expand-routes ~routes ~dna))))

(defmacro defrouter
  "Expands a sequence of user-defined routes and creates
   a router from those routes.

   Equivalent to (def ... (create-router (expand-routes ...))),
   but expands routes at compile time. You want routes expanded
   at compile time, so if you wish to use create-router directly,
   use it with defroutes which will also expand routes at compile
   time."
  [name routes & opts]
  (let [settings (get-settings opts)
        dna (prepare-dna routes/default-dna settings)]
    `(def ~name
       (create-router (expand-routes ~routes ~dna) ~settings))))
