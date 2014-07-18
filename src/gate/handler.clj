(ns gate.handler
  (:refer-clojure :exclude [read])
  (:require [gate.handler.param :as param]
            [ring.util.response :as ring]))


;; Much of this namespace taken from or inspired
;; by Compojure (compojure.core)


(defn read-param-as
  "When there's a param and you know how to read it,
   read it. If you can't read it, give it back."
  [param-type param]
  
  (let [read (get param/readers param-type)] 
    (if (and read param)
      (read param)
      param)))


(defn get-param
  "Get a param matching sym (as a keyword or string)
   from the :params map on a Ring request."
  [req sym]
  
  `(get-in ~req [:params ~(keyword sym)]
                (get-in ~req [:params ~(str sym)])))


(defn assoc-symbol-as
  "Attempts to convert the parameter matching sym
   into the indicated type, and associate the result
   with sym in the bindings."
  [bindings req [sym ptype]]
  
    (assoc bindings sym `(read-param-as ~ptype ~(get-param req sym))))


(defn assoc-symbol
  "Associates the parameter matching sym with sym
   in the bindings."
  [bindings req sym]
  
  (assoc bindings sym (get-param req sym)))


(defn vector-bindings
  "Take a vector representing a number of symbols and
   create a map representing the binding of each symbol
   to the associated param from the request.

   These params can be converted to a non-string type
   before binding, and the whole request can also be
   bound to a symbol that follows they keyword :as."
  [args req]
  
  (loop [args args
         bindings {}]
    (if-let [sym (first args)]
      (cond
       (= :as sym)
         (recur (nnext args) (assoc bindings (second args) req))
         
       (vector? sym)
         (recur (next args) (assoc-symbol-as bindings req sym))
         
       (symbol? sym)
         (recur (next args) (assoc-symbol bindings req sym))
         
       :else
         (throw (Exception. (str "Unexpected binding: " sym))))
      
      (mapcat identity bindings))))


(defmacro let-request
  [[bindings request] & body]
  
  (if (vector? bindings)
    `(let [~@(vector-bindings bindings request)] ~@body)
    `(let [~bindings ~request] ~@body)))


(defn compile-handler
  [handler]
  
  (fn [request]
    (handler request)))


(defn create-handler
  [bindings body]
  
  `(compile-handler
    (fn [request#]
      (let-request [~bindings request#] ~@body))))
