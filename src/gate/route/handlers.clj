(ns gate.route.handlers
  (:require [gate.route.query-string :as query-string]))

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

(defn expand-handlers
  [route]
  (when-let [handlers (get route :handlers)]
    (let [r (dissoc route :handlers)
          middleware (get route :middleware)]
      (for [[method handler] handlers]
          (let [handler (read-fn handler)
                action (read-fn (create-action handler middleware))]
            (assoc r :method method
                   :handler handler
                   :action action))))))
