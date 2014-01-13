(ns gate.handler
  (:refer-clojure :exclude [read])
  (:require [gate.handler.param :as param]))

;; Much of this namespace taken or adapted from
;; Compojure (compojure.core)

(defn ^:private read-param-as
  [param-type param]
  (let [read (get param/readers param-type)]
    (when (and read param)
      (read param))))

(defn ^:private get-param
  [req sym]
  `(get-in ~req [:params ~(keyword sym)]
                (get-in ~req [:params ~(str sym)])))

(defn ^:private assoc-symbol-as
  [bindings req [sym ptype]]
    (assoc bindings sym `(read-param-as ~ptype ~(get-param req sym))))

(defn ^:private assoc-symbol
  [bindings req sym]
  (assoc bindings sym (get-param req sym)))

(defn ^:private vector-bindings
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

(defmacro ^:private let-request [[bindings request] & body]
  (if (vector? bindings)
    `(let [~@(vector-bindings bindings request)] ~@body)
    `(let [~bindings ~request] ~@body)))

(defn ^:private compile-handler
  [handler]
  (fn [request]
    (handler request)))

(defn ^:private create-handler
  [bindings body]
  `(compile-handler (fn [request#]
                      (let-request [~bindings request#] ~@body))))

(defmacro defhandler
  "Convinience function for constructing ring handlers.

   Using a vector of symbols for req-bindings binds names from
   the :params key of a ring request map, though the whole request
   can be bound by adding ':as' to req-bindings vector and following
   it with whatever symbol you want the request bound as.

   Using a symbol for req-bindings binds the whole request to
   that symbol."
  [name req-bindings & body]
  `(def ~name
     ~(create-handler req-bindings body)))
