(defproject gate "0.0.18"
  :description "Gate is a routing library for Clojure's Ring."
  :url "https://github.com/mischov/gate"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/tools.macro "0.1.2"]
                 [ring/ring-core "1.2.1"]
                 [potemkin "0.3.11"]]
  :plugins [[lein-expectations "0.0.7"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.6.0"]
                                  [expectations "1.4.52"]
                                  [compojure "1.1.6"]
                                  [criterium "0.4.3"]]
                   :warn-on-reflection true}})
