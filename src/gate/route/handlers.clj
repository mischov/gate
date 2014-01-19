(ns gate.route.handlers
  (:require [gate.route.middleware :as middleware]
            [gate.response :as response]))

(def request-methods #{:get :post :head :put :delete
                       :trace :connect :options :any})

(defn ^:private handler->action
  "An action differs from a handler in that, while a
   handler can return anything, an action should return
   a Ring response."
  [handler]
  (fn [request]
    (response/render (handler request) request)))

(defn ^:private create-action
  "Creates an action from a handler and wraps that in
   middleware."
  [handler middlewares]
    (let [action (handler->action handler)
          wrapped-action (middleware/wrap-action action middlewares)]
      (fn [request]
        (wrapped-action request))))

(defprotocol Handler
  (read-handler [handler middleware]))

(extend-protocol Handler
  clojure.lang.IFn
  (read-handler
    [handler middlewares]
    {:handler handler
     :action (create-action handler middlewares)})
  clojure.lang.IPersistentMap
  (read-handler
    [handler middlewares]
    (let [h (get handler :handler)
          m (get handler :middleware)]
        {:handler h
         :action (create-action h (concat middlewares m))})))

(defn expand-handlers
  "Creates an expanded route method in route."
  [route]
  (when-let [handlers (filter #(request-methods (first %))  route)]
    (let [r (into {} (filter #(not (request-methods (first %))) route))
          middlewares (get route :middleware)]
      (for [[method handler] handlers]
          (let [rh (read-handler handler middlewares)]
            (merge r rh {:method method}))))))
