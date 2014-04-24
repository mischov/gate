(ns gate.router.preware
  (:require [gate.middleware :refer [combine]]))


(defn get-preware
  "Preware fns are applied to a request before it is routed.

   Unless this is absolutely the behavior required, you
   should use middleware instead.

   These functions accept a Ring request and return a Ring
   request."
  [settings]
  (when-let [preware (get settings :preware)]
    (apply comp (combine preware []))))
