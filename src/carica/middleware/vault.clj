(ns carica.middleware.vault
  "Middleware to replace values in a config map with those found at the
  given path in Vault.  Expects the environment to already contain
  VAULT_ADDR and VAULT_TOKEN.

  e.g.,
  (vault-substitute-config
    \"/secret/some/path/here:keyname\"
    :config :path)"
  (:require
   [carica.middleware :as mw]))

(def vault-addr
  "The Vault address"
  (mw/getenv "VAULT_ADDR"))

(def vault-token
  "A valid Vault token"
  (mw/getenv "VAULT_TOKEN"))

(def vault-availible?
  "Determine if vault-clj is available."
  (try
    (require 'vault.core)
    (require 'vault.client.http)
    (let [ok (atom true)]
      (when-not vault-addr
        (println "VAULT_ADDR not set in the environment.")
        (reset! ok false))
      (when-not vault-token
        (println "VAULT_TOKEN not set in the environment.")
        (reset! ok false))
      ok)
    (catch Throwable _
      (println "vault-clj not found.")
      false)))

;; These shims are necessary due to how vault.core is required.  There
;; is probably a better way.

(defn new-client
  "Generates a new Vault client map."
  [& args]
  (apply (ns-resolve (symbol "vault.core") (symbol "new-client"))
         args))

(defn authenticate!
  "Authenticates the Vault client with Vault."
  [& args]
  (apply (ns-resolve (symbol "vault.core") (symbol "authenticate!"))
         args))

(defn read-secret
  "Reads a secret from Vault and returns a map with the value.  :data is
  the default key if the value wasn't a map."
  [& args]
  (apply (ns-resolve (symbol "vault.core") (symbol "read-secret"))
         args))

(defn vault-substitute-config
  "Middleware that will override a known key with the value found at the
  specified `vault-path` in Vault.  The `vault-path` is in the form of
  PATH:key.  The :key may be omitted when the value is a simple string.

  Given this config map:
  {:something 1
   :otherthing 2}

  Given this middleware definition:
  (vault-substitute-config \"secret/path/of/my/key:keyname\" :otherthing)

  Given an vaule in vault at that path like (json):
  {\"keyname\": 5}

  This config will result:
  {:something 1
   :otherthing 5}"
  [vault-path & keyseq]
  {:pre [vault-available?]}
  (let [        [vault-path secret-key] (string/split vault-path #":" 2)
        secret-key (if secret-key (keyword secret-key) :data)
        client (doto (new-client vault-addr)
                 (authenticate! :token vault-token))]
    (fn [f]
      (fn [resources]
        (let [cfg-map (f resources)]
          (assoc-in cfg-map keyseq
                    (get (read-secret client vault-path) secret-key)))))))
