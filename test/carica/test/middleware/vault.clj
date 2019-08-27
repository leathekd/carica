(ns carica.test.middleware.vault
  (:require [clojure.test :refer :all]
            [carica.core :refer [configurer resources config]]
            [carica.middleware.vault :refer :all]
            [vault.core :as vault]
            [vault.client.mock]))

(deftest test-vault-substitute-config-middleware
  (testing "vault substitution occurs"
    (with-redefs [vault-client (fn []
                                 (vault/new-client "mock:test/secrets.edn"))]
      (let [test-config (configurer
                         (resources "config.clj")
                         [(vault-substitute-config "vault/path:key" :magic-word)])]
        (is (= "Now." (test-config :magic-word))
            "Should see our value from Vault.")))))
