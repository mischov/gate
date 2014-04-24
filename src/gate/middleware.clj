(ns gate.middleware
  (:import [clojure.lang Fn Sequential]))


(defprotocol Middleware
  "Middleware can either be a Ring middleware function
   or a seq of Ring middleware functions."
  
  (combine [middleware middlewares]))


(extend-protocol Middleware
  Sequential
  (combine [ms middlewares]
    (loop [m (first ms)
           ms (next ms)
           result middlewares]
      (if-not m
        result
        (recur (first ms) (next ms) (combine m result)))))
  
  Fn
  (combine [m middlewares]
    (if (some #{m} middlewares)
      middlewares
      (conj middlewares m))))


(defn wrap-handler
  [handler middlewares]
  (let [middlewares (combine middlewares [])
        wrap (apply comp middlewares)]
    (if wrap
      (wrap handler)
      handler)))
