(ns gate.routes.path
  (:require [clojure.string :as string])
  (:import [java.util.regex Pattern]))

;; Taken (mostly) from Pedestal (io.pedestal.service.http.route)

(defn ^:private parse-path-token
  "Parses path-token and updates appropriate values in result."
  [result string]
  (condp re-matches string
    #"^:(.+)$" :>> (fn [[_ token]]
                     (let [key (keyword token)]
                       (-> result
                           (update-in [:path-parts] conj key)
                           (update-in [:path-params] conj key)
                           (assoc-in [:path-constraints key] "([^/]+)"))))
    #"^\*(.+)$" :>> (fn [[_ token]]
                      (let [key (keyword token)]
                        (-> result
                            (update-in [:path-parts] conj key)
                            (update-in [:path-params] conj key)
                            (assoc-in [:path-constraints key] "(.*)"))))
    (update-in result [:path-parts] conj string)))

(defn parse-path
  "Parses path to update route's DNA with current route's `:path-parts`,
   `:path-params`, `:path-constraints`."
  ([pattern parent-dna]
     (if-let [m (re-matches #"/(.*)" pattern)]
       (let [[_ path] m]
         (reduce parse-path-token
                 parent-dna
                 (string/split path #"/")))
       (throw (ex-info "Invalid route pattern" {:pattern pattern})))))

(defn ^:private make-path-regex
  [route]
  (let [{:keys [path-parts path-constraints]} route]
    (re-pattern
     (apply str
       (interleave (repeat "/")
                   (map #(or (get path-constraints %) (Pattern/quote %))
                        path-parts))))))

(defn add-path-regex
  [route]
  (assoc route :path-re (make-path-regex route)))

(defn expand-path
  [route route-dna]
  (let [r (merge route route-dna)]
    (add-path-regex r)))
