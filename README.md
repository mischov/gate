# Gate

Gate is an HTTP routing library for Ring and Clojure.

```clojure
(ns hello-world
  (:require [gate :refer [handler defrouter]]))

(defrouter app
  [{:path "/"
    :get  (handler [] "<h1>Hello, World!</h1>")}]
  {:not-found (handler [] "<h1>404</h1><p>Not Found</p>")}) 
```

## Contents

- [Installation](#installation)
- [Rationale](#rationale)
- [Status](#status)
- [Quick Start](#quick-start)
- [API](#api)
    - [Routes](#routes)
    - [defrouter](#defrouter)
    - [handler and defhandler](#handler-and-defhandler)
- [Documentation](#documentation)
- [Performance](#performance)
- [Acknowledgements](#acknowledgements)

## Installation

Add the following dependency to your `project.clj` file:

```clojure
[gate "0.0.17"]
```
[**Back To Top ⇧**](#contents)

## Rationale

#### Gate:

1. Represents routes as data instead of as macros.
1. Decomplects handler creation from route creation.
1. Allows easy application of middleware to both individual routes and groups of routes.
1. Stays fast, even when routing over large sets of routes.

Read more [here](https://github.com/mischov/gate/wiki/Rationale).

[**Back To Top ⇧**](#contents)

## Status

Gate is in **early alpha**.

Both the API and the concepts underlying the library are subject to change, so it is not yet advisable to use Gate in production.

Gate does work, however, and is waiting for you to experiment with it.

[**Back To Top ⇧**](#contents)

## Quick Start

To see Gate in action, check the [hello-world-example](https://github.com/mischov/gate/blob/master/examples/gate/examples/hello_world_example.clj) and [other examples](https://github.com/mischov/gate/tree/master/examples/gate/examples).

[**Back To Top ⇧**](#contents)

## API

In order to get started with Gate, you only need to know:

1. [How to define routes.](#routes)
2. [How to create a router.](#defrouter)
3. [How to reduce handler boilerplate.](#defhandler)

### Routes

Routes in Gate are just Clojure maps containing some combination of the following keys:

| Key | Required? | Value Type | Description |
| --- | --------- | ---------- | ----------- |
| `:path` | Required | `String` | The route's path (or a portion thereof). Gate paths support `:variables` and `*splats`. |
| `:middleware` | Optional | `[Middleware]` | A seq containing [middleware](https://github.com/mischov/gate/wiki/Middleware) to be applied to the route's handlers. |
| `:children`   | Optional | `[Route]` | A seq containing other routes. Child routes inherit their parent's path and middleware, which is then combined with their own path and middleware. |
| `:get` `:post` `:head` `:put` `:delete` `:trace` `:connect` `:options` `:any` | Optional | `Handler` | HTTP method keys are each paired with a Gate handler, which is any function that accepts a Ring request and returns something. Pairing a method key with a handler defines how the route will react to that particular HTTP request method. Routes may contain multiple method keys (ie, both :get and :post). |
   
```clojure
(def paradoxical-routes
  {:path "/simple-route"
   :get  (hander [] "I am a simple route.")
   :children [
              {:path "/inversion"
               :get (handler [] "I am not a simple route.")}]})

; Defines routes that match GET requests to "/simple-route"
; and "/simple-route/inversion".
```

For more about routes, check [wiki/Routes](https://github.com/mischov/gate/wiki/Routes).

[**Back To Top ⇧**](#contents)

### defrouter

Provided a router name, a sequence of routes, and optionally a map of router settings, `defrouter` binds a router to the router name. A router, in turn, accepts a Ring request and returns an appropriate Ring response.

```clojure
(defrouter app
  [{:path "/"
    :get  (handler [] "I'm an index.")}]
  {:not-found (handler [] "I can't find what you're looking for.")})

; (app {:request-method :get :uri "/"})
; > {:status 200, :headers {"Content-Type" "text/html; charset=utf-8"},
;    :body "I'm an index."}

; (app {:request-method :get :uri "/the-answer-to-life-and-everything"})
; > {:status 404, :headers {"Content-Type" "text/html; charset=utf-8"},
;    :body "I can't find what you're looking for."}
```

For a full list of possible router settings, check [wiki/Router-Settings](https://github.com/mischov/gate/wiki/Router-Settings).

[**Back To Top ⇧**](#contents)

### handler and defhandler

A Gate handler is just any function that accepts a Ring request and returns something.

```clojure
(defn birthday-greeter
  [request]

  (let [username (get-in request [:params :username])
        age (Long/parseLong (get-in request [:params :age]))]
	(str "Happy birthday, " username "! "
	     "It seems like just yesterday you were " (- age 1) "....")))
```

However, the boilerplate from getting and coercing params really adds up, so Gate provides the convenience macros `handler` and `defmacro`.

These macros will be (partially) familiar to Compojure users since they reimplement much of Compojure's request-map destructuring.


```clojure
(defhandler birthday-greeter
  [username age :- Long]
   
  (str "Happy birthday, " username "! "
       "It seems like just yesterday you were " (- age 1) "...."))
```

The example above illustrates one of the most useful differences between `handler`/`defhandler` and Compojure, which is that `handler`/`defhandler` allow you to coerce parameters from strings to other types of data at the time that they are extracted from :params.

```clojure
(defhandler arithmetic
  [op
   n1 :- Number
   n2 :- Number]
   
  (case op
    "add" (str (+ n1 n2))
    "sub" (str (- n1 n2))
    "mult" (str (* n1 n2))
    "div" (str (/ n1 n2))
    (str "Operation '" op "' not recognized.")))

(defrouter app
  [{:path "/:op/:n1/:n2"
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

For more about `handler`/`defhandler` or param coercion, see [wiki/Handlers](https://github.com/mischov/gate/wiki/Handlers).

[**Back To Top ⇧**](#contents)

## Documentation

Additional documentation is a work in progress, but that which exists can be found in the [wiki](https://github.com/mischov/gate/wiki).

## Performance

Unscientific testing suggests that Gate's routing performance is similar to Compojure's for applications with small numbers of routes.

For applications with large numbers of routes, Gate's trie-based router appears to perform better than routing libraries which use the more traditional "check the request against a list of all possible routes and return the first match" approach.

[**Back To Top ⇧**](#contents)

## Acknowledgements

Gate owes a lot to Compojure and Pedestal.

[**Back To Top ⇧**](#contents)

## License

Copyright © 2014 Mischov

Distributed under the Eclipse Public License.
