(ns gate.routes.dna
  (:require [gate.routes.path :refer [parse-path]]))

(def blank-dna
  "DNA is information used by routes during expansion.

   Routes pass their DNA on to their children."
  {:path nil
   :path-parts []
   :path-params []
   :middleware []})

(defn ^:private update-dna-path
  "Initializes a route's dna by combining parent-dna with
   information from the route's path."
  [parent-dna route]
  (let [p (get route :path)]
    (parse-path p parent-dna)))

(defn ^:private update-dna-middleware
  "Combines route and parent middleware then adds the result
   to the current route's dna."
  [route-dna route]
  (let [m  (get route :middleware)
        pm (get route-dna :middleware)]
    (assoc route-dna :middleware (concat pm m))))

(defn create-route-dna
  "A route's dna is created by updating it's parent-dna
   with information from a route's path and middleware."
  [route parent-dna]
  (-> parent-dna
      (update-dna-path route)
      (update-dna-middleware route)))

(defn init-dna
  "A router's base-dna is created by combining either the
   base-dna defined in router-settings or blank-dna with
   any router-wide middleware defined in router-settings."
  [router-settings]
  (let [base-dna (get router-settings :base-dna blank-dna)]
    (if-let [middleware (get router-settings :middleware)]
      (assoc base-dna :middleware middleware)
      base-dna)))
