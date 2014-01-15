# Gate

Gate is a web routing library for Ring and Clojure.

```clojure
(ns hello-world
  (:require [gate :refer [defrouter]]))

(defrouter app
  [{:name :hello
    :path "/"
    :get  (fn [_] "<h1>Hello, World!</h1>")}]
  {:on-404 (fn [_] "<h1>404</h1><p>Not Found</p>")}) 
```

## Contents

- [Installation](#installation)
- [Rationale](#rationale)
- [Status](#status)
- [Quick Start](#quick-start)
- [Documentation](#documentation)
- [Performance](#performance)
- [Acknowledgements](#acknowledgements)

## Installation

Add the following dependency to your `project.clj` file:

```clojure
[gate "0.0.5"]
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
  (:require [gate :refer [defrouter]]
            [gate.handler :refer [defhandler]]))

;; A handler is some function that accepts a Ring request.
;; Gate tries to turn whatever it returns into a Ring response.

(defn hello-handler
  [req]
  "Hello, World!")

;; Since it's no fun to manually pull params out of requests,
;; Gate also provides a convinence macro, defhandler, to make
;; it easy to access parameters.

(defhandler greeter
  [name]
  (str "Hello, " name "!"))

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
    :get  hello-handler
    :post (fn [_] "None of that now, you hear!")}
   {:name :hello-person
    :path "/:name"
    :middleware [enthusiator]
    :get  greeter}])

;; A router takes a sequence of routes and an optional map
;; of router settings.

(defrouter router
  quickstart-routes
  {:on-404 (fn [_] "There's nothing here....")
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

Documentation is a work in progress, but what exists can be found in the Wiki.

- [Wiki](https://github.com/mischov/gate/wiki).
    - [Routes](https://github.com/mischov/gate/wiki/Routes)
    - [Handlers](https://github.com/mischov/gate/wiki/Handlers)
    - [Middleware](https://github.com/mischov/gate/wiki/Middleware)
    - [Resources](https://github.com/mischov/gate/wiki/Resources)

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
