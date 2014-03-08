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

(defn recursive-merge
  [& vals]
  (if (every? map? vals)
    (apply merge-with recursive-merge vals)
    (last vals)))

(defn routes->trie
  [routes]
  (loop [route (first routes)
         routes (next routes)
         trie {}]
    (if-not route
      trie
      (recur (first routes)
             (next routes)
             (recursive-merge (route->trie route) trie)))))

(defn get-last-splat
  [splat last-splat params segments]
  (if splat
    (conj splat {:params params :segments segments})
    last-splat))

(defn search-trie
  [trie segments]
  (loop [segment (first segments)
         segments segments
         trie trie
         lsplat nil
         params []]
    (if-not segment
      [(get trie :methods) params]
      (let [literal (get trie segment)
            variable (get trie :var)
            splat (get trie :splat)
            last-splat (get-last-splat splat lsplat params segments)]
        (cond
         (empty? segment) (recur (fnext segments)
                                 (next segments)
                                 trie
                                 lsplat
                                 params)
         literal (recur (fnext segments)
                        (next segments)
                        literal
                        last-splat
                        params)
         variable (recur (fnext segments)
                         (next segments)
                         variable
                         last-splat
                         (conj params segment))
         splat [(get splat :methods)
                (conj params (string/join "/" segments))]
         lsplat [(get lsplat :methods)
                 (conj (get lsplat :params)
                       (string/join "/" (get lsplat :segments)))]
         :else nil)))))
