(ns gate.handler.params
  (:require [clojure.string :refer [lower-case]]
            [clojure.edn])
  (:import [clojure.lang Keyword]))


(deftype Edn [v])


(defmacro try*
  [& body]
  
  (let [try-body (butlast body)
        [catch sym & catch-body :as batch-form] (last body)]
    (assert (= catch 'catch))
    (assert (symbol? sym))
    `(try ~@try-body (~'catch Throwable ~sym ~@catch-body))))


(defn safely
  [f x]
  (try* (f x)
        (catch e x)))


(defmulti parse-param (fn [t _] t))


(defmethod parse-param Keyword
  [_ param]
  
  (if (string? param)
    (keyword (lower-case param))
    param))


(defmethod parse-param Boolean
  [_ param]

  (if (string? param)
    (Boolean/parseBoolean param)
    param))


(defmethod parse-param Integer
  [_ param]
  
  (safely #(Integer/parseInt %) param))


(defmethod parse-param Long
  [_ param]

  (safely #(Long/parseLong %) param))


(defmethod parse-param Double
  [_ param]

  (safely #(Double/parseDouble %) param))


(defn read-number
  [s]

  (let [n (clojure.edn/read-string s)
        l (safely long n)]
    (if (== n l)
      l
      n)))

(defn parse-number
  [^String s]

  (cond
   (.contains s ".") (Double/parseDouble s)
   :else             (try* (Long/parseLong s)
                           (catch e (safely read-number s)))))


(defmethod parse-param Number
  [_ param]

  (safely parse-number param))


(defmethod parse-param Edn
  [_ param]

  (if (string? param)
    (clojure.edn/read-string param)
    param))
