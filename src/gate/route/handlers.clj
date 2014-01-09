(ns gate.route.handlers
  (:require [gate.middleware :as middleware]))

(def request-methods #{:get :post :head :put :delete
                       :trace :connect :options :any})

(defn create-action
  [handler middlewares]
  (let [wrapped-handler (middleware/wrap-handler handler middlewares)]
    (fn [req]
      (wrapped-handler req))))

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
  [route]
  (when-let [handlers (filter #(request-methods (first %))  route)]
    (let [r (into {} (filter #(not (request-methods (first %))) route))
          middlewares (get route :middleware)]
      (for [[method handler] handlers]
          (let [rh (read-handler handler middlewares)]
            (merge r rh {:method method}))))))
