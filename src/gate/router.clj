(ns gate.router
  (:require [gate.router.trie :as trie]
            [gate.router.urls :refer [create-url-builder]]
            [gate.router.resources :refer [get-resource-matching]]
            [gate.util.response.not-found :refer [add-not-found]]))

(defn get-route-matching
  [{:keys [uri request-method] :as request} router-trie]
  (let [segments (trie/split-path uri)
        [methods raw-params] (trie/search-trie router-trie segments)]
    (when methods
      (when-let [[handler path-params] (get methods request-method)]
        (let [params (zipmap path-params raw-params)
              req (conj request {:params params
                                 :path-params params})]
          (handler req))))))

(defn create-router
 ([routes] (create-router routes {}))
 ([routes settings]
      (let [router-trie (trie/routes->trie routes)
            url-builder (create-url-builder routes)]
        (fn [request]
          (let [request (-> request
                            (assoc :url-builder url-builder)
                            (add-not-found settings))]
            (or (get-resource-matching request settings)
                (get-route-matching request router-trie)
                (:not-found request)))))))
