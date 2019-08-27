(defproject leathekd/carica "1.3.1-SNAPSHOT"
  :description "A flexible configuration library"
  :url "https://github.com/leathekd/carica"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.8.3"

  :dependencies [[cheshire "5.8.1"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.clojure/tools.reader "1.3.2"]]

  :profiles {:dev
             {:resource-paths ["etc"]
              :dependencies [[org.clojure/clojure "1.10.1"]]}
             :vault
             {:resource-paths ["etc"]
              :dependencies [[amperity/vault-clj "0.7.0"]]}}

  :deploy-repositories {"releases" {:url "https://repo.clojars.org"
                                    :username [:gpg :env/clojars_username]
                                    :password [:gpg :env/clojars_password]}
                        "snapshots" {:url "https://repo.clojars.org"
                                     :username [:gpg :env/clojars_username]
                                     :password [:gpg :env/clojars_password]}}
  :release-tasks
  [["clean"]
   ["vcs" "assert-committed"]
   ["test" ":all"]
   ["clean"]
   ["change" "version" "leiningen.release/bump-version" "release"]
   ["vcs" "commit"]
   ["vcs" "tag" "--no-sign"]
   ["deploy" "clojars"]
   ["change" "version" "leiningen.release/bump-version"]
   ["vcs" "commit" "Next development version %s"]
   ["vcs" "push" "origin" "HEAD"]])
