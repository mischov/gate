(ns gate.router.trie
  (:require [clojure.string :as string]))

(defn split-path
  [^String path]
  (string/split path #"/"))

(defn create-node
  [^String segment children]
  (if (empty? segment)
    children
    (case (.charAt segment 0)
      \: (assoc {} :var children)
      \* (assoc {} :splat children)
      (assoc {} segment children))))

(defn build-trie
 [[seg & segs] method route]
 (if segs
   (create-node seg (build-trie segs method route))
   (create-node seg {:methods {method route}})))

(defn route->trie
  [{:keys [path method path-params handler] :as route}]
  (let [segments (split-path path)]
    (build-trie segments method [handler path-params])))

(defn routes->trie
  [routes]
  (loop [route (first routes)
         routes (next routes)
         trie {}]
    (if-not route
      trie
      (recur (first routes)
             (next routes)
             (merge-with (partial merge-with merge) (route->trie route) trie)))))

(defn search-trie
  [trie segments]
  (loop [segment (first segments)
         segments (next segments)
         trie trie
         params []]
    (if-not segment
      [(get trie :methods) params]
      (let [empty (empty? segment)
            literal (get trie segment)
            variable (get trie :var)
            splat (get trie :splat)]
        (cond
         empty (recur (first segments)
                      (next segments)
                      trie
                      params)
         literal (recur (first segments)
                        (next segments)
                        literal
                        params)
         variable (recur (first segments)
                         (next segments)
                         variable
                         (conj params segment))
         splat [(get splat :methods)
                (conj params (string/join "/" (cons segment segments)))]
         :else nil)))))
