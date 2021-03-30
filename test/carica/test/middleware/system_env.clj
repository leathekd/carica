(ns carica.test.middleware.system-env
  (:require [carica.core :refer [configurer resources]]
            [carica.middleware.system-env :as senv]
            [clojure.test :refer :all]))

(deftest load-system-env
  (testing "it only uses environment variables who's path is present in the config map"
    (let [cfg-map {:bone-saw "not ready!"}
          sys-env {"BONE_SAW" "ready!" "NEED_COFFEE" "false"}
          result (((senv/load-system-env sys-env) identity) cfg-map)]
      (is (= result {:bone-saw "ready!"}))))

  (testing "it converts environment variables containing __ into nested config values"
    (let [cfg-map {:bone {:saw "not ready!"}}
          sys-env {"BONE__SAW" "ready!" "NEED__COFFEE" "false"}
          result (((senv/load-system-env sys-env) identity) cfg-map)]
      (is (= result {:bone {:saw "ready!"}}))))

  (testing "it converts boolean and numerical values"
    (let [cfg-map {:bone {:saw nil}
                   :need {:coffee nil}}
          sys-env {"BONE__SAW" "1" "NEED__COFFEE" "false"}
          result (((senv/load-system-env sys-env) identity) cfg-map)]
      (is (= result {:bone {:saw 1}
                     :need {:coffee false}}))))

  (testing "it works as middleware"
    (let [test-config (configurer
                       (resources "config.clj")
                       [(senv/load-system-env {"NESTED_MULTI_CLJ__TEST_CLJ__TEST_CLJ" "new value"
                                               "FROM_TEST" "yep!"})])]
      (is (= "new value" (test-config :nested-multi-clj :test-clj :test-clj)))
      (is (= "yep!" (test-config :from-test))))))
