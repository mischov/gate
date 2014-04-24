(ns gate.handler.param
  (:require [clojure.tools.reader.edn :as edn]))


(defn read-number
  "If `s` appears to be a number, read-number will attempt
   to read it.

   If the first character of your string is a zero and the
   string does not contain a decimal point, this function
   (like Clojure) will attempt to read your number as an
   octal. If you were not trying to pass this function an
   octal then you will be displeased by the result."
  [^String s]
  (cond
   (re-find #"^-?\d+\.?\d*$" s)
     (edn/read-string s)
   
   (re-find #"^\.\d+$" s)
     (edn/read-string (str "0" s))
   
   :else nil))


(defn read-integer
  "Reads a string representing an integer (the mathematical
   concept, not the type) into a long.

   Yes, integers are parsed into longs. Clojure uses longs
   to represent all integers smaller than bigints, so it
   seemed like a good idea to follow suit."
  [^String s]
  (when (re-find #"^\d+$" s)
    (Long/parseLong s)))


(defn read-decimal
  "Reads a string representing a decimal into a double."
  [^String s]
  (when (re-find #"^\d*\.\d+$" s)
    (Double/parseDouble s)))


(defn read-keyword
  "Reads a string representing a keyword into a lowercase
   keyword."
  [^String s]
  (keyword (clojure.string/lower-case s)))


(def readers
  {:num read-number
   :int read-integer
   :decimal read-decimal
   :keyword read-keyword})
