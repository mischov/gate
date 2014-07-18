(ns gate.examples.hello-world-example
  (:require [gate :refer [defrouter defhandler handler]]))

;; A Gate handler is some function that accepts a Ring request.
;;
;; Gate tries to coerce whatever the handler returns into
;; a Ring response.

(defn hello-world
  [request]
  "Hello, World!")

;; Because manually pulling parameters out of a request
;; can get tedious, gate provides a convenience macro,
;; defhandler, to make it easier.
;;
;; In the following example, defhandler gets the :name key
;; from the request :params and binds it to the symbol `name`.

(defhandler hello-name
  [name]
  (str "Hello, " name "!"))

;; There is also an anonymous version (ie, fn vs defn) of
;; defhandler named handler.

;; A Gate router takes a list of routes and an optional map
;; of options and returns a function that attempts to find
;; a route matching a Ring request.

(defrouter app
  [{:name :hello-world
    :path "/hello"
    :get hello-world
    :children [
               {:name :hello-name
                :path "/:name"
                :get hello-name}]}]
  {:404-handler (handler [] "Nothing here to see! Carry on!")})

;; (app {:uri "/hello" :request-method :get})
;; => {:status 200, :headers {"Content-Type" "text/html; charset=utf-8"}, :body "Hello, World!"}

;; (app {:uri "/hello" :request-method :post})
;; => {:status 404, :headers {"content-type" "text/html; charset=utf-8"}, :body "Nothing here to see! Carry on!"}

;; (app {:uri "/hello/Clojure" :request-method :get})
;; => {:status 200, :headers {"Content-Type" "text/html; charset=utf-8"}, :body "Hello, Clojure!"}
