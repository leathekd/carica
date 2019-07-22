(defproject carica "1.2.3"
  :description "A flexible configuration library"
  :url "https://github.com/leathekd/carica"
  :dependencies [[cheshire "5.8.1"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.clojure/tools.reader "1.3.2"]]
  :profiles {:dev
             {:resource-paths ["etc"]
              :dependencies [[org.clojure/clojure "1.10.1"]]}})
