# Gate

Gate is a web routing library for Ring and Clojure.

```clojure
(ns hello-world
  (:require [gate :refer [defrouter]]))

(defrouter app
  [{:name :hello
    :path "/"
    :handlers {:get (fn [_] "<h1>Hello, World!</h1>")}}}]
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
- [Acknowledgements](#acknowledgements)

## Installation

Add the following dependency to your `project.clj` file:

```clojure
[gate "0.0.1"]
```
[**Back To Top ⇧**](#contents)

## Rationale

Clojure data structures make better routes than Clojure macros.

Using data structures for routes not only makes it easier to generate, nest, combine, and otherwise manipulate routes (you've got a whole language of tools at your disposal), but also makes it easier to apply middleware granularly (and will make it easier to implement reverse routing in the future).

Some libraries (such as Polaris and Pedestal) are already taking advantage of the benefits gained by using data structures to represent routes, but they tend to favor vector-based routes ahead of map-based routes.

Gate favors map-based routes, which are more explicit and hopefully more maintainable.

Gate also plays very nicely with existing ring middleware.

[**Back To Top ⇧**](#contents)

## Status

Gate is in early alpha. Both the API and the concepts underlying the library are subject to change, so it is not yet advisable to use Gate for serious projects.

That said, feel free to experiment with Gate and report bugs or make suggestions.

[**Back To Top ⇧**](#contents)

## Quick Start

```clojure
(ns yourproj.quickstart
  (:require [gate :refer [defrouter]]))

;; A handler is some function that accepts a request.
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
    :handlers {:get (fn [_] "Hello, World!")}}
   {:name :hello-person
    :path "/:name"
    :middleware [enthusiator]
    :handlers {:get greeter}}])

;; A router takes a sequence of routes and an optional map
;; of router settings.
(defrouter router
  quickstart-routes
  {:on-404 (fn [_] "There's nothing there....")})

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

A Gate route is a Clojure map consisting of two required fields (`:name` and `:path`) and three optional fields (`:middleware`, `:handlers`, and `children`).

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

  :middleware
    (Optional, [Fn])
    A sequence of uncalled functions. More specificially, a
    sequence of uncalled ring middleware functions.
    
  :handlers
    (Optional, {Keyword, Fn})
    A map of keywords representing http methods (ie. :get :post),
    to uncalled functions representing route handlers.

    Route handlers accept a request.

  :children
    (Optional, [Concise-Route-Map])
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
   :handlers {:get view-post}
   :children [{:name :edit-post
   	       :path "/edit"
	       :middleware [require-admin]
	       :handlers {:get view-edit-post
	         	  :post edit-post}}]})

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

## Acknowledgements

Gate is fortunate enough to be built upon the shoulders of Compojure and Pedestal, and couldn't ask for a truer pair of giants. 

[**Back To Top ⇧**](#contents)

## License

Copyright © 2014 Mischov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
