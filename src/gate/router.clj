(ns gate.router
  (:require [gate.response :as response]))

(defn ^:private shares-method?
  [request-method]
  (fn [route]
    (let [route-method (get route :method)]
      (or (= route-method :any)
          (= request-method route-method)
          false))))

(defn ^:private expand-path-params
  [path-params]
  {:params path-params :path-params path-params})

(defn ^:private find-matching
  [request {:keys [matcher action] :as route}]
  (when-let [path-params (matcher request)]
    (let [r (merge-with merge request (expand-path-params path-params))]
      (response/render (action r) r))))

(defn issue-404
  [handler request]
   (let [handler (if (fn? handler) (handler request) handler)]
     {:status 404
      :headers {"content-type" "text/html; charset=utf-8"}
      :body handler}))

(defn create-router
  "Accepts a sequence of expanded routes and an optional map of
   options and returns a router.

   Options:
             :not-found - "
  ([routes] (create-router routes {}))
  ([routes {:keys [on-404]
            :or {on-404 "404: Not Found"}
            :as settings}]
     (fn [request]
       (let [request-method (get request :request-method)
             routes (filter (shares-method? request-method) routes)]
         (or (some #(find-matching request %) routes)
             (issue-404 on-404 request))))))


