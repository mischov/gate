(ns gate.routes.path
  (:require [clojure.string :as string])
  (:import [java.util.regex Pattern]))


; Originally adapted from Pedestal.


(defn ^:private parse-path-token
  "Parses path-token and updates appropriate values in result."
  [result string]
  
  (condp re-matches string
    #"^:(.+)$" :>>
      (fn [[_ token]]
        (let [key (keyword token)]
          (-> result
              (update-in [:path-parts] conj key)
              (update-in [:path-params] conj key))))
      
    #"^\*(.+)$" :>>
      (fn [[_ token]]
        (let [key (keyword token)]
          (-> result
              (update-in [:path-parts] conj key)
              (update-in [:path-params] conj key))))

    #"^([^\*]+)\*$" :>>
      (fn [[_ const]]
        (-> result
            (update-in [:path-parts] conj const)))

    #"^([^\*]+)\*(.+)$" :>>
      (fn [[_ const token]]
        (let [key (keyword token)]
          (-> result
              (update-in [:path-parts] conj const key)
              (update-in [:path-params] conj key))))
      
    (update-in result [:path-parts] conj string)))


(defn parse-path
  "Parses path to update route's DNA with current route's
  `:path-parts`, `:path-params`, `:path-constraints`, and
  `:path`."
  ([pattern parent-dna]
     
     (if-let [m (re-matches #"/(.*)" pattern)]
       (let [[_ path] m]
         (reduce parse-path-token
                 (update-in parent-dna [:path] str "/" path)
                 (string/split path #"/")))
       (throw (ex-info "Invalid route pattern" {:pattern pattern})))))
