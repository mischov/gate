(ns gate.router.urls)

(defn ^:private get-url-fns
  "Takes a seq of expanded routes and returns a map of each
   route name to the appropriate url-creating function"
  [routes]
  (loop [routes routes
         route (first routes)
         result {}]
    (if-not route
      result
      (let [{:keys [name url-fn]} route]
        (recur (next routes) (first routes) (assoc result name url-fn))))))

(defn create-url-builder
  "Creates a function that attempts to build a url from a
   route name and a map of params."
  [routes]
  (let [url-fns (get-url-fns routes)]
    (fn [route-name params]
      (when-let [url-fn (get url-fns route-name)]
        (url-fn params)))))

(defn add-url-builder
  "Adds an app's url-builder to a ring request as :url-builder."
  [request url-builder]
  (assoc request :url-builder url-builder))
