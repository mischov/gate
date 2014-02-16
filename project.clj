(defproject gate "0.0.10"
  :description "Gate is a routing library for Clojure's Ring."
  :url "https://github.com/mischov/gate"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [expectations "1.4.52"]
                 [ring/ring-core "1.2.1"]
                 [org.clojure/tools.reader "0.8.3"]]
  :plugins [[lein-expectations "0.0.7"]])
