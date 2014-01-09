(ns gate.route.handlers)

(def request-methods #{:get :post :head :put :delete
                       :trace :connect :options})

(defn read-fn
  [f]
  (cond
   (symbol? f) @(resolve f)
   (seq? f) (eval f)
   (fn? f) f))

(defn create-action
  [handler middleware]
  (let [wrapper (apply comp middleware)
        wrapped-handler (wrapper handler)]
    (fn [req]
      (wrapped-handler req))))

(defprotocol Handler
  (read-handler [handler middleware]))

(extend-protocol Handler
  clojure.lang.IFn
  (read-handler
    [handler middleware]
    {:handler handler
     :action (create-action handler middleware)})
  clojure.lang.APersistentMap
  (read-handler
    [handler middleware]
    (let [h (get handler :handler)
          m (get handler :middleware)]
        {:handler h
         :action (create-action h (concat middleware m))})))

(defn expand-handlers
  [route]
  (when-let [handlers (filter #(request-methods (first %))  route)]
    (let [r (into {} (filter #(not (request-methods (first %))) route))
          middleware (get route :middleware)]
      (for [[method handler] handlers]
          (let [rh (read-handler handler middleware)]
            (merge r rh {:method method}))))))
