(ns gate.router
  (:require [gate.util.response.not-found :refer [add-not-found]]
            [gate.router.urls :refer [add-url-builder]]
            [gate.router.resources :refer [get-resource-matching]]
            [gate.routes :as routes]))

(defn ^:private has-method?
  "Returns true if route's method matches request method,
   or if route's method is :any."
  [request-method route]
  (let [route-method (get route :method)]
    (or (= route-method :any)
        (= request-method route-method)
        false)))

(defn ^:private route-matches
  [request {:keys [matcher handler] :as route}]
  (when-let [path-params (matcher request)]
    (let [req (merge-with merge request path-params)]
      (handler req))))

(defn ^:private get-route-matching
  "Returns first route matching the request, or nil
   if no route matches."
  [request routes]
  (let [request-method (get request :request-method)
        routes (filter #(has-method? request-method %) routes)]
    (some #(route-matches request %) routes)))

(defn create-router
  "Accepts a sequence of expanded routes and an optional map of
   options and returns a router."
  ([routes] (create-router routes {}))
  ([routes settings]
     (fn [request]
       (let [request (-> request
                         (add-url-builder routes)
                         (add-not-found settings))]
         (or (get-resource-matching request settings)
             (get-route-matching request routes)
             (:not-found request))))))
