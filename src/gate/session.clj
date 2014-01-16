(ns gate.session
  "Stateful sessions for gate apps."
  (:refer-clojure :exclude [get get-in remove swap!])
  (:require [ring.middleware.session :as rs]
            [ring.middleware.session.cookie :as cooksess]
            [ring.middleware.session.memory :as memsess]
            [gate.middleware :as middleware]))

(def cookie-store cooksess/cookie-store)
(def memory-store memsess/memory-store)

;; Most of the rest of this library is copied from
;; lib-noir's noir.session.

(declare ^:dynamic *session*)

(defn set
  "Sets key in session to value."
  [key value]
  (clojure.core/swap! *session* assoc key value))

(defn get
  "Gets value of key from session if key is present,
   otherwise returns default value or nil if no
   default value is specified."
  ([key] (get key nil))
  ([key default]
     (clojure.core/get @*session* key default)))

(defn get-in
  "Gets value associated with path of keys if key
   is present, otherwise returns default value or
   nil if no default value is specified."
  ([keys] (get-in keys nil))
  ([keys default]
     (clojure.core/get-in @*session* keys default)))

(defn swap!
  "Applies function with optional args to *session*."
  [function & args]
  (apply clojure.core/swap! *session* function args))

(defn destroy
  "Destroys current session."
  []
  (reset! *session* {}))

(defn remove
  "Removes key from session."
  [key]
  (clojure.core/swap! *session* dissoc key))

(defn ^:private get-gate-session
  "Gets Gate's session from a Ring :session map."
  [r]
  (clojure.core/get-in r [:session :gate] {}))

(defn gate-session
  "Stores the Gate session as :gate in Ring's
   :session map. This keeps Gate's stateful session
   from interfering with any other middleware that
   depends on ring sessions.

   Ring understands (not (contains? response :session))
   to mean 'don't update the session'.

   Ring understands (nil? (:session response)) to mean
   'delete the session'."
  [handler]
  (fn [request]
    (binding [*session* (atom (get-gate-session request))]
      (when-let [resp (handler request)]
        (if (= (get-gate-session resp) *session*)
          resp
          (if (contains? resp :session)
            (if (nil? (:session resp))
              resp
              (assoc-in resp [:session :gate] @*session*))
            (assoc resp :session (assoc (:session request)
                                        :gate @*session*))))))))

(defn add-ring-session
  ([] (add-ring-session {}))
  ([opts]
     #(rs/wrap-session % opts)))

(defn add-session
  ([] (add-session {}))
  ([opts]
     (middleware/combine #(rs/wrap-session % opts) gate-session)))
