(ns gate.middleware
  (:require [gate.middleware.session :as gs]
            [gate.middleware.app :as ga]))

(def add-ring-session gs/add-ring-session)
(def api ga/api)
