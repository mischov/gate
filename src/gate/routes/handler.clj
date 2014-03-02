(ns gate.routes.handler
  (:require [gate.middleware :as middleware]
            [gate.util.response :as response]))

(def request-methods #{:get :post :head :put :delete
                       :trace :connect :options :any})

(defn ^:private wrap-handler
  "Wraps handler with middleware after ensuring that it
   returns a Ring response."
  [handler middlewares]
    (let [handler (fn [request] (response/render (handler request) request))
          wrapped-handler (middleware/wrap-handler handler middlewares)]
      (fn [request]
        (wrapped-handler request))))

(defprotocol Handler
  (read-handler [handler middleware]))

(extend-protocol Handler
  clojure.lang.IFn
  (read-handler
    [handler middlewares]
    {:handler-fn (.getName (class handler))
     :handler (wrap-handler handler middlewares)})
  clojure.lang.IPersistentMap
  (read-handler
    [handler middlewares]
    (let [h (get handler :handler)
          m (get handler :middleware)]
        {:handler-fn (.getName (class h))
         :handler (wrap-handler h (concat middlewares m))})))

(defn expand-handlers
  "Creates an expanded route method in route."
  [route]
  (when-let [handlers (filter #(request-methods (first %))  route)]
    (let [r (into {} (filter #(not (request-methods (first %))) route))
          middlewares (get route :middleware)]
      (for [[method handler] handlers]
          (let [rh (read-handler handler middlewares)]
            (merge r rh {:method method}))))))
