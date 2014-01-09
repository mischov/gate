(ns gate.response
  (:require [ring.util.response :refer [response content-type]])
  (:import [java.io File InputStream]
           [clojure.lang IFn ISeq]))

;; Taken mostly from Compojure (compojure.response)

(defprotocol Renderable
  (render [this request]
    "Render the object into a form suitable for a given request."))

(extend-protocol Renderable
  nil
  (render [_ _] nil)
  String
  (render [body _] (-> (response body)
                       (content-type "text/html; charset=utf-8")))
  IFn
  (render [func request] (render (func request) request))
  File
  (render [file _] (response file))
  ISeq
  (render [coll _] (-> (response coll)
                       (content-type "text/html; charset=utf-8")))
  InputStream
  (render [stream _] (response stream)))
