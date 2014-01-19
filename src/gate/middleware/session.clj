(ns gate.middleware.session
  (:require [ring.middleware.session :as rs]
            [ring.middleware.session.memory :as rsm]
            [ring.middleware.session.cookie :as rsc]))

(def memory-store rsm/memory-store)
(def cookie-store rsc/cookie-store)

(defn add-ring-session
  ([] (add-ring-session {}))
  ([opts]
     #(rs/wrap-session % opts)))
