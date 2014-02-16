(ns gate.routes.matcher)

(defn ^:private path-matcher
  [route]
  (let [{:keys [path-re path-params]} route]
    (fn [request]
      (when request
        (when-let [m (re-matches path-re (get request :uri))]
          (zipmap path-params (rest m)))))))

(defn ^:private matcher
  [route]
  (let [path-match (path-matcher route)]
    (fn [request]
      (when-let [path-params (path-match request)]
        {:params path-params
         :path-params path-params}))))

(defn add-matcher
  [route]
  (let [m (matcher route)]
    (assoc route :matcher m)))
