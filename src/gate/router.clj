(ns gate.router
  (:require [gate.router.trie :as trie]
            [gate.router.resources :refer [get-resource-matching]]
            [gate.router.preware :refer [get-preware]]
            [gate.util.response.not-found :refer [add-not-found]]))

(defn get-route-matching
  [{:keys [uri request-method] :as request} router-trie]
  (let [segments (trie/split-path uri)
        [methods raw-params] (trie/search-trie router-trie segments)]
    (when methods
      (when-let [[handler path-params] (or (get methods request-method)
                                           (get methods :any))]
        (let [params (zipmap path-params raw-params)
              req (conj request {:params params
                                 :path-params params})]
          (handler req))))))

(defn create-router
 ([routes] (create-router routes {}))
 ([routes settings]
      (let [router-trie (trie/routes->trie routes)
            preware (get-preware settings)]
        (fn [request]
          (let [request (-> (if preware (preware request) request)
                            (add-not-found settings))]
            (or (get-resource-matching request settings)
                (get-route-matching request router-trie)
                (:not-found request)))))))
