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
[gate "0.0.8"]
```
[**Back To Top ⇧**](#contents)

## Rationale

1. Clojure data structures make better routes than Clojure macros.

   Using data structures for routes not only makes it easier to generate, nest, combine, and otherwise manipulate routes (you've got a whole language of tools at your disposal), but also makes it easier to apply middleware granularly (and will make it easier to implement reverse routing in the future).

2. Maps are clearer representations of routes than vectors.
   
   Some libraries (such as Polaris and Pedestal) are already taking advantage of the benefits gained by using data structures to represent routes, but they tend to favor vector-based routes ahead of map-based routes.

  Gate favors map-based routes, whose explicit nature is hopefully more maintainable.

3. Granular application of middleware is really useful.
  
   Gate not only plays nicely with existing ring middleware, but attempts to improve the experience by allowing users to easily apply a middleware to a single route or even a single method of a route (in addition to being able to add middleware to an whole app or route hierarchy).

[**Back To Top ⇧**](#contents)

## Status

Gate is in early alpha. Both the API and the concepts underlying the library are subject to change, so it is not yet advisable to use Gate for serious projects.

That said, feel free to experiment with Gate and report bugs or make suggestions.

[**Back To Top ⇧**](#contents)

## Quick Start

Import `defrouter` and `defhandler` from gate and gate.handler.

```clojure
(ns yourproj.quickstart
  (:require [gate :refer [defrouter]]
            [gate.handler :refer [defhandler]]))
```

A Gate handler is some function that accepts a Ring request. Gate attempts to convert whatever a handler returns into a Ring response.

```clojure
(defn hello-handler
  [request]
  "Hello, World!")
```

Since it's no fun to manually pull params out of the request in every handler, Gate also provides a convinence macro, `defhandler`, to make accessing params easier. In the following example, `defhandler` pulls the key `:name` out of the :params map of the Ring request and binds it to the symbol `name`.

```clojure
(defhandler greeter
  [name]
  (str "Hello, " name "!"))
```

Gate middleware is just standard Ring middleware, so it accepts a handler and returns some function which accepts a request.

```clojure
(defn enthusiator
  [handler]
  (fn [req]
    (let [r (update-in req [:params :name] clojure.string/upper-case)
          resp (handler r)
	  body (get resp :body)]
	  (assoc resp :body (str body "!!"))))
```

A Gate route is a map containing the keys :name and :path, optionally one or more keys representing HTTP methods paired with a handler, and optionally the keys :middleware and :children.

```clojure
(def quickstart-routes
  [{:name :hello-world
    :path "/"
    :get  hello-handler
    :post (fn [_] "None of that now, you hear!")}
   {:name :hello-person
    :path "/:name"
    :middleware [enthusiator]
    :get  greeter}])
```

The heart of a gate application is the router. A router takes a sequence of Gate routes and an optional map of router settings and returns a function that attempts to match a Ring request to a handler.

```clojure
(defrouter router
  quickstart-routes
  {:404-handler (fn [_] "There's nothing here....")
   :resources {:path "/" :root "public"}})
```

Routers are used like any other Ring handler.

For example, to run the previous example with lein-ring you would set `:plugins` key of your project.clj to `[[lein-ring "0.8.10"]]` and the `:ring` key to `{:handler yourproj.quickstart/router}.`

[**Back To Top ⇧**](#contents)

## Documentation

Documentation is a work in progress, but what exists can be found in the [Wiki](https://github.com/mischov/gate/wiki):

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
