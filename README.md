# Gate

Gate is a web routing library for Ring and Clojure.

```clojure
(ns hello-world
  (:require [gate :refer [defrouter]]))

(defrouter app
  [{:name :hello
    :path "/"
    :get  (fn [_] "<h1>Hello, World!</h1>")}]
  {:404-handler (fn [_] "<h1>404</h1><p>Not Found</p>")}) 
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
[gate "0.0.12"]
```
[**Back To Top ⇧**](#contents)

## Rationale

In an attempt to advance the state of Clojure url routing, Gate:

1. Represents routes as data structures instead of macros.
1. Allows simple application of middleware to both individual routes and groups of routes.
1. Routes via a trie router, providing faster routing for large route sets.
1. Promotes clear, maintainable code.

Read more [here](https://github.com/mischov/gate/wiki/Rationale).

[**Back To Top ⇧**](#contents)

## Status

Gate is in early alpha. Both the API and the concepts underlying the library are subject to change, so it is not yet advisable to use Gate for serious projects.

That said, please feel free to experiment with Gate and report bugs or make suggestions.

[**Back To Top ⇧**](#contents)

## Quick Start

```clojure
(ns yourproj.quickstart
  (:require [gate :refer [defrouter]]
            [gate.handler :refer [defhandler]]))

; A Gate handler is some function that accepts a Ring request.
; Gate attempts to convert whatever a handler returns into a
; Ring response.

(defn hello-handler
  [req]
  "Hello, World!")

; Since it's no fun to manually pull params out of the request 
; in every handler, Gate also provides a convinence macro,
; defhandler, to make accessing params easier.
;  
; In the following example, the key :name is retrieved from
; the :params map of the request and bound to the symbol 'name'.

(defhandler greeter
  [name]
  (str "Hello, " name "!"))

; Gate middleware is just standard Ring middleware, so it accepts
; a handler and returns some function accepting a request.

(defn enthusiator
  [handler]
  (fn [req]
    (let [r (update-in req [:params :name] clojure.string/upper-case)
          resp (handler r)
          body (get resp :body)]
      (assoc resp :body (str body "!!"))))

; A gate route is a map containing the keys :name and :path, maybe
; the keys :middleware and :children, and maybe one or more keys
; representing an HTTP method paired with a handler.

(def quickstart-routes
  [{:name :hello-world
    :path "/"
    :get  hello-handler
    :post (fn [_] "None of that now, you hear!")}
   {:name :hello-person
    :path "/:name"
    :middleware [enthusiator]
    :get  greeter}])

; The heart of a Gate app is the router, which takes a sequence
; of routes and an optional map of router settings and returns a
; function that attempts to match a Ring request to a handler.

(defrouter router
  quickstart-routes
  {:404-handler (fn [_] "There is nothing here....")
   :resources {:path "/" :root "public"}})

; Run routers like you would run any ring handler.
; 
; For instance, if you wanted to run this with lein-ring you'd
; set the following keys in your defproject:
;
;   :plugins [[lein-ring "0.8.10"]]
;   :ring {:handler yourproj.quickstart/router}
```
[**Back To Top ⇧**](#contents)

## Documentation

Documentation is a work in progress, but what exists can be found in the [Wiki](https://github.com/mischov/gate/wiki):

- [Routes](https://github.com/mischov/gate/wiki/Routes)
- [Handlers](https://github.com/mischov/gate/wiki/Handlers)
- [Middleware](https://github.com/mischov/gate/wiki/Middleware)
- [Resources](https://github.com/mischov/gate/wiki/Resources)

[**Back To Top ⇧**](#contents)

## Performance

Unscientific testing suggests that Gate's routing performance is similar to Compojure's for applications with small numbers of routes.

For applications with large numbers of routes, Gate's trie-based router appears to perform better than routing libraries which use the more traditional "check the request against a list of all possible routes and return the first match" approach.

[**Back To Top ⇧**](#contents)

## Acknowledgements

Gate is fortunate enough to be built upon the shoulders of Compojure and Pedestal, and couldn't ask for a truer pair of giants. 

[**Back To Top ⇧**](#contents)

## License

Copyright © 2014 Mischov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
