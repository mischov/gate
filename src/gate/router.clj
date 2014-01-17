(ns gate.router
  (:require [gate.response :as response]
            [gate.urls :refer [add-url-builder]]
            [gate.response.not-found :refer [add-not-found]]
            [ring.util.response :refer [resource-response]]
            [ring.middleware.content-type :refer [content-type-response]]))

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

(defn ^:private set-content-type
  [response request]
  (let [resp (content-type-response response request)]
    (when (get resp :status)
      resp)))

(defn ^:private resource-matcher
  [request resource-settings]
  (let [uri (get request :uri)]
    (when (re-find #"\.[A-Za-z]+" uri)
        (let [default-settings {:path "/" :root "public"}
              {:keys [path root]} (merge default-settings
                                         resource-settings)
              full-path (str root "/" (clojure.string/replace-first uri
                                                                    path
                                                                    ""))]
          (-> (resource-response full-path)
              (set-content-type request)
              )))))

(defn ^:private find-matching
  [request {:keys [matcher action] :as route}]
  (when-let [path-params (matcher request)]
    (let [r (merge-with merge request (expand-path-params path-params))]
      (response/render (action r) r))))

(defn create-router
  "Accepts a sequence of expanded routes and an optional map of
   options and returns a router."
  ([routes] (create-router routes {}))
  ([routes {:keys [resources] :as settings}]
     (fn [request]
       (let [request (-> request
                         (add-url-builder routes)
                         (add-not-found settings))
             request-method (get request :request-method)
             routes (filter (shares-method? request-method) routes)]
         (or (when resources (resource-matcher request resources))
             (some #(find-matching request %) routes)
             (:not-found request))))))
