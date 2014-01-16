(ns gate.handler
  (:refer-clojure :exclude [read])
  (:require [gate.handler.param :as param]
            [gate.urls :as urls]
            [ring.util.response :as ring]))

;; Much of the rest of this namespace taken or adapted
;; from Compojure (compojure.core)

(defn read-param-as
  "When there's a param and you know how to read it,
   read it. If you can't read it, give it back."
  [param-type param]
  (let [read (get param/readers param-type)]
    (if (and read param)
      (read param)
      param)))

(defn  get-param
  "Get a param matching sym (as a keyword or string)
   from the :params map on a Ring request."
  [req sym]
  `(get-in ~req [:params ~(keyword sym)]
                (get-in ~req [:params ~(str sym)])))

(defn  assoc-symbol-as
  "Attempts to convert the parameter matching sym
   into the indicated type, and associate the result
   with sym in the bindings."
  [bindings req [sym ptype]]
    (assoc bindings sym `(read-param-as ~ptype ~(get-param req sym))))

(defn  assoc-symbol
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

(defmacro let-request [[bindings request] & body]
  (if (vector? bindings)
    `(let [~@(vector-bindings bindings request)] ~@body)
    `(let [~bindings ~request] ~@body)))

(defn compile-handler
  [handler]
  (fn [request]
    (handler request)))

(defn create-handler
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

;; Handler Redirection

(defn redirect
  "This isn't the redirect you're looking for.

   HTTP 302 should be used when you want to retry an
   exact request (method and all) at a different url.
   Many browers do not implement 302 behavior this way,
   instead attempting to make a GET request for the
   new url. This means that the status code is a legacy
   tool more than anything else.

   If you wish to GET to a different handler after
   performing some action, use `then-redirect` instead.

   Can be provided with a string representing a url
   or can create a url from the name of a route and
   optionally a map of path parameters (if they are
   needed to construct the url)."
  ([url]
     (ring/redirect url))
  ([request route-name & [params]]
     (let [url (urls/build-url request route-name params)]
       (ring/redirect url))))

(defn then-redirect
  "Returns an HTTP 303, instructing the browser to GET
   the provided url.

   Useful for, among other things, when you want to
   send a user somewhere else after you do something
   with the POST request that they just submitted.

   Can be provided with a string representing a url
   or can create a url from the name of a route and
   optionally a map of path parameters (if they are
   needed to construct the url)."
  ([url]
     (ring/redirect-after-post url))
  ([request route-name & [params]]
     (let [url (urls/build-url request route-name params)]
       (ring/redirect-after-post url))))


