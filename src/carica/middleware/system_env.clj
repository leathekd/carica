(ns carica.middleware.system-env
  (:require [clojure.edn :as edn]
            [clojure.string :as string]))

(defn str->num [s]
  "Convert numeric string into `java.lang.Long` or `clojure.lang.BigInt`"
  (try
    (Long/parseLong s)
    (catch NumberFormatException _
      (bigint s))))

(defn str->value [v]
  (cond
    (re-matches #"[0-9]+" v) (str->num v)
    (re-matches #"^(true|false)$" v) (Boolean/parseBoolean v)
    (re-matches #"\w+" v) v
    :else
    (try
      (let [parsed (edn/read-string {:readers *data-readers*} v)]
        (if (symbol? parsed)
          v
          parsed))
      (catch Throwable _
        v))))

(defn k->path
  [k dash level]
  (as-> k $
    (string/lower-case $)
    (string/split $ level)
    (map (comp keyword #(string/replace % dash "-")) $)))

(defn contains-in?
  "Checks whether `path` exists within `m`.
  An empty path always returns true which is akin to the behavior of `get-in`."
  [m [first & rest :as path]]
  (if (empty? path)
    true
    (and (contains? m first) (contains-in? (get m first) rest))))

(defn substitute [cfg-map [k-path v]]
  (if (and (seq k-path)
           (contains-in? cfg-map k-path))
    (assoc-in cfg-map k-path v)
    cfg-map))

(defn load-system-env
  "This middleware converts system environment variables into a map
  who's values are merged into the main config IF the path exists

  ex:

  export BONE_SAW=\"ready!\"

  ((load-system-env identity) {:bone-saw \"not ready!\"})
  => {:bone-saw \"ready!\"}

  Environment variables are converted to lower-case and dasherized.
  Environment variables can map to nested values in the config map by
  separating keys with 2 underscore characters ex:

  MY__NESTED__VALUE=\"nice\" => {:my {:nested {:value \"nice\"}}}"
  ([] (load-system-env (System/getenv)))
  ([env]
   (fn [f]
     (fn [resources]
       (let [cfg-map (f resources)
             sys-map (->> env
                          (map (fn [[k v]] [(k->path k "_" #"__")
                                            (str->value v)]))
                          (into {}))]
         (reduce substitute cfg-map sys-map))))))
