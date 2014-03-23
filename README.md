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
    - [API](#api)
         - [Route Structure](#route-structure)
	 - [`defrouter`](#defrouter)
	 - [`defhandler`](#defhandler)
- [Performance](#performance)
- [Acknowledgements](#acknowledgements)

## Installation

Add the following dependency to your `project.clj` file:

```clojure
[gate "0.0.13"]
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

To see Gate in action, check the [hello-world-example](https://github.com/mischov/gate/blob/master/examples/gate/examples/hello_world_example.clj) and the other [examples](https://github.com/mischov/gate/tree/master/examples/gate/examples).

[**Back To Top ⇧**](#contents)

## Documentation

Documentation is a work in progress, but that which exists can be found in the [wiki](https://github.com/mischov/gate/wiki).

### API

In order to get started with Gate, you only need to know:

1. Route Structure
2. `defrouter`
3. `defhandler`

#### Route Structure

Routes in Gate are just Clojure maps containing some combination of the following keys:

| Key | Required? | Value Type | Description |
| --- | --------- | ---------- | ----------- |
| `:name` | Required | `Keyword` | The route's name, used for reverse-routing. |
| `:path` | Required | `String` | The route's path (or a portion thereof). Gate paths support `:variables` and `*splats`. |
| `:middleware` | Optional | `[Middleware]` | A seq containing middleware to be applied to the route's handlers. |
| `:children`   | Optional | `[Route]` | A seq containing other routes. Child routes inherit their parent's path and middleware, which is then combined with their own path and middleware. |
| `:get` `:post` `:head` `:put` `:delete` `:trace` `:connect` `:options` `:any` | Optional | `Handler` | These HTTP method keys are paired with a Gate handler, which is any function that accepts a Ring request and returns something. Pairing a method key with a handler defines how the route will react to that particular HTTP request method. Routes allow multiple method keys (ie, both :get and :post). |
   
```clojure
(def paradoxical-routes
  {:name :simple-route
   :path "/simple-route"
   :get  (fn [_] "I am a simple route.")
   :children [
              {:name :complex-route
               :path "/inversion"
               :get (fn [_] "I am not a simple route.")}]})

; Defines routes that match GET requests to "/simple-route"
; and "/simple-route/inversion".
```

For more about routes, check the [wiki](https://github.com/mischov/gate/wiki/Routes).

[**Back To Top ⇧**](#contents)

#### `defrouter`

Provided a router name, a sequence of routes, and optionally a map of router settings, defrouter (`gate/defrouter`) binds a router to the router name. A router, in turn, accepts a Ring request and returns an appropriate Ring response.

```clojure
(defrouter app
  [{:name :index
    :path "/"
    :get  (fn [_] "I'm an index.")}]
  {:404-handler (fn [_] "I can't find what you're looking for.")})

; (app {:request-method :get :uri "/"})
; > {:status 200, :headers {"Content-Type" "text/html; charset=utf-8"},
;    :body "I'm an index."}

; (app {:request-method :get :uri "/the-answer-to-life-and-everything"})
; > {:status 404, :headers {"Content-Type" "text/html; charset=utf-8"},
;    :body "I can't find what you're looking for."}
```

For a full list of possible router settings, check the [wiki](https://github.com/mischov/gate/wiki/Router-Settings).

[**Back To Top ⇧**](#contents)

#### `defhandler`

A Gate handler is just any function that accepts a Ring request and returns something.

```clojure
(defn personalized-greeter
  [request]
  (let [visitor-name (get-in request [:params :visitor-name])]
    (str "Hello, " visitor-name "!")))
```

However, manually pulling the params out of the request like that becomes very old, very fast.

To combat all that boilerplate, Gate introduces the convenience macro defhandler (`gate.handler/defhandler`). Defhandler will be familiar to Compojure users since it reimplements much Compojure's request-map destructuring, but it is subtly different (and also decomplected out of route definition).

```clojure
(defhandler personalized-greeter
  [visitor-name]
  (str "Hello, " visitor-name "!"))
```

One of the most important differences between defhandler and Compojure is that defhandler allows parameters to be coerced from strings to other types of data at the time that they are extracted from :params.

```clojure
(defhandler arithmetic
  [op [n1 :num] [n2 :num]]
  (case op
    "add" (str (+ n1 n2))
    "sub" (str (- n1 n2))
    "mult" (str (* n1 n2))
    "div" (str (/ n1 n2))
    (str "Operation '" op "' not recognized.")))

(defrouter app
  [{:name :arith
    :path "/:op/:n1/:n2"
    :get  arithmetic}])

; (app {:request-method :get :uri "/add/2/2"})
; > {:status 200, :headers {"Content-Type" "text/html; charset=utf-8"},
;    :body "4"}

; (app {:request-method :get :uri "/div/2/2"})
; > {:status 200, :headers {"Content-Type" "text/html; charset=utf-8"},
;    :body "1"}

; (app {:request-method :get :uri "/raise/2/2"})
; > {:status 200, :headers {"Content-Type" "text/html; charset=utf-8"},
;    :body "Operation 'raise' not recognized."}
```

For more about defhandler and param coercion, see the [wiki](https://github.com/mischov/gate/wiki/Handlers).

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

Distributed under the Eclipse Public License.
