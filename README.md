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

Distributed under the Eclipse Public License.
