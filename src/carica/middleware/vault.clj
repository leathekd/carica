(ns carica.middleware.vault
  (:require [carica.middleware :as mw]
            [clojure.string :as string]
            [vault.core :as vault]))

(defn vault-addr*
  "Retrieves the Vault address from the VAULT_ADDR environment variable."
  []
  (System/getenv "VAULT_ADDR"))

(def vault-addr (memoize vault-addr*))

(defn vault-token*
  "Retrieves the Vault token from the VAULT_TOKEN environment variable."
  []
  (System/getenv "VAULT_TOKEN"))

(def vault-token (memoize vault-token*))

(defn vault-client
  "Returns an authenticated vault client."
  []
  (doto (vault/new-client (vault-addr))
    (vault/authenticate! :token (vault-token))))

(defn vault-substitute-config
  "Overrides a specified key with the value found at the given location in vault. The vault location coordinates are of the form PATH:KEY. If the path is a flat scalar string value (e.g. vault would return {\"data\": \"myvalue\"}) it can be omitted from the location string.

  Args: vault coordinate string, configuration key(s)

  Example:
  {:something 1
   :otherthing 2}

  Given this middleware:
  (vault-substitute-config \"secret/path/to/my/value:keyname\" :otherthing)

  Given Vault contains this json at the above path:
  {\"keyname\": 5}

  The resulting config is now:
  {:something 1
   :otherthing 5}
  "

  [vault-path & keyseq]
  (let [[vault-path secret-key] (string/split vault-path #":" 2)
        secret-key (if secret-key (keyword secret-key) :data)
        client (vault-client)]
    (fn [f]
      (fn [resources]
        (let [cfg-map (f resources)]
          (assoc-in cfg-map keyseq
                    (get (vault/read-secret client vault-path) secret-key)))))))
