# Gate

Gate is a web routing library for Ring and Clojure.

```clojure
(ns hello-world
  (:require [gate :refer [defrouter]]))

(defrouter app
  [{:name :hello
    :path "/"
    :get  (fn [_] "<h1>Hello, World!</h1>")}}]
  {:on-404 (fn [_] "<h1>404</h1><p>Not Found</p>")}) 
```

## Contents

- [Installation](#installation)
- [Rationale](#rationale)
- [Status](#status)
- [Quick Start](#quick-start)
- [Documentation](#documentation)
    - [Routes](#routes)
    - [Middleware](#middleware)
    - [Resources](#resources)
- [Performance](#performance)
- [Acknowledgements](#acknowledgements)

## Installation

Add the following dependency to your `project.clj` file:

```clojure
[gate "0.0.3"]
```
[**Back To Top ⇧**](#contents)

## Rationale

1. Clojure data structures make better routes than Clojure macros.

   Using data structures for routes not only makes it easier to generate, nest, combine, and otherwise manipulate routes (you've got a whole language of tools at your disposal), but also makes it easier to apply middleware granularly (and will make it easier to implement reverse routing in the future).

2. Maps are clearer representations of routes than vectors.
   
   Some libraries (such as Polaris and Pedestal) are already taking advantage of the benefits gained by using data structures to represent routes, but they tend to favor vector-based routes ahead of map-based routes.

  Gate favors map-based routes, whose explicit nature is hopefully more maintainable.

3. Granular application of middleware is really useful.
  
   Gate not only plays very nicely with existing ring middleware, it attempts to improve the experience by allowing users to easily apply a middleware to a single route or even a single method of a route, in addition to being able to apply middleware to a whole app or group of routes. Gate also makes it easy to combine middleware that needs to be used together into a single middleware.

[**Back To Top ⇧**](#contents)

## Status

Gate is in early alpha. Both the API and the concepts underlying the library are subject to change, so it is not yet advisable to use Gate for serious projects.

That said, feel free to experiment with Gate and report bugs or make suggestions.

[**Back To Top ⇧**](#contents)

## Quick Start

```clojure
(ns yourproj.quickstart
  (:require [gate :refer [defrouter]]))

;; A handler is some function that accepts a Ring request.
;; Gate tries to turn whatever it returns into a Ring response.
(defn greeter
  [req]
  (let [name (get-in req [:params :name])]
    (str "Hello, " name "!")))

;; Middleware is standard Ring middleware, it accepts
;; a handler and returns a function that accepts a request.
(defn enthusiator
  [handler]
  (fn [req]
    (let [r (update-in req [:params :name] clojure.string/upper-case)
          resp (handler r)]
	  (str resp "!!"))))

;; Routes are just a sequence of route-maps.
(def quickstart-routes
  [{:name :hello-world
    :path "/"
    :get  (fn [_] "Hello, World!")
    :post (fn [_] "None of that now, you hear!")}
   {:name :hello-person
    :path "/:name"
    :middleware [enthusiator]
    :get  greeter}])

;; A router takes a sequence of routes and an optional map
;; of router settings.
(defrouter router
  quickstart-routes
  {:on-404 (fn [_] "There's nothing there....")
   :resources {:path "/" :root "public"}})

;; Run routers like you would run any ring handler.
;; For instance, if you wanted to run this with lein-ring you'd
;; set the following keys in your defproject:
;;
;;   :plugins [[lein-ring "0.8.10"]]
;;   :ring {:handler yourproj.quickstart/router}
```
[**Back To Top ⇧**](#contents)

## Documentation

### Routes

A Gate route is a Clojure map consisting of two required fields (`:name` and `:path`), a variable number of optional request-method fields (ie `:get`, `:post`, etc) and two other optional fields (`:middleware` and `children`).

```
Route

  :name
    (Required, Keyword)
    The name of the route, used to reverse engineer urls.

  :path
    (Required, String)
    A string representing the path of the route. Must begin with
    "/". Follows convention that "/:..." matches a sub-path, and
    "/*..." is a catch-all.

  :get|:post|:head|:put|:delete|:trace|:connect|:options|:any
    (Optional, Fn|{:handler Fn :middleware [Fn]})
    A keyword representing an HTTP method is paired with either
    an uncalled function representing a handler, or a map with
    the keys :handler and :middleware. :handler is an uncalled
    handler function, :middleware is a sequence of uncalled ring
    middleware functions.

    More than one of http-method key can be included in a
    route.
  
  :middleware
    (Optional, [Fn])
    A sequence of uncalled ring middleware functions. 

  :children
    (Optional, [Route])
    A seq of other routes. Children share path and middleware with
    their parents.
```
[**Back To Top ⇧**](#contents)

### Middleware

Currently Gate middleware is just standard ring middleware, but Gate does a few things which makes it even nicer to use.

```clojure

;; A lot of the time you'll find ring middleware used like this:

(defn standard-middleware-wrapper
  [handler]
  (-> handler
      add-authorization-middleware ; requires session middleware
      add-session-middleware))
      
;; This works, but it could be improved upon.

;; Firstly, when you wrap a handler that way, it's in reverse order.
;; Authorization-middleware requires session-middleware, but
;; authorization-middleware is listed first. In Gate, dependence is
;; top to bottom or left to right, so it reads more logically.

(def gate-middleware
   [add-session-middleware add-authorization-middleware])

;; Next, because each route map in gate has its own list of
;; middleware, it's easy to apply middleware to only the routes
;; you want it applied to.

;; In this example, accessing "/gate-rocks" is open to anybody,
;; while accessing "/gate-rocks/edit" invokes the middleware
;; require-admin.
(def post-routes
  {:name :view-post
   :path "/:post-id"
   :get  view-post
   :children [{:name :edit-post
               :path "/edit"
               :middleware [require-admin]
               :get  view-edit-post
               :post edit-post}]})

;; Child routes also inherit middleware from their parents.
;; In the example above, if :view-post had the middleware
;; [log-view], :edit-post would have the middleware
;; [log-view require-admin]

;; gate.middleware also includes a convinience macro, defcomp,
;; that combines middleware often used together to a single
;; middleware.

;; This defcomp, also part of gate.middleware, is the equivalent
;; of Compojure's compojure.handler/api.
(defcomp api
  wrap-params
  wrap-nested-params
  wrap-keyword-params)

;; Finally, if you want to apply middleware to all routes in
;; a router, the seq of that middleware can be defined as
;; :middleware in the settings map of create-router or defrouter. 
  
```
[**Back To Top ⇧**](#contents)

### Resources

Gate does not route to static resources by default.

If you'd like Gate to route to static resource, add a `:resources` map to the settings map of either `defrouter` or `create-router`.

The `:resources` key can contain a `:path` key and a `:root` key, but even just pairing `:resources` with an empty map will instruct Gate to serve static resources.

Unless told otherwise, Gate assumes that you are serving resources from path "/" and that those resources can be found in resources/public (which is :root "public").

[**Back To Top ⇧**](#contents)

## Performance

A couple of entirely unscientific `ab` tests suggest that Gate and Compojure have very similar performance when it comes to routing a request.

[**Back To Top ⇧**](#contents)

## Acknowledgements

Gate is fortunate enough to be built upon the shoulders of Compojure and Pedestal, and couldn't ask for a truer pair of giants. 

[**Back To Top ⇧**](#contents)

## License

Copyright © 2014 Mischov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
