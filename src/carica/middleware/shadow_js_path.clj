(ns carica.middleware.shadow-js-path
  (:require [carica.core :refer [load-with]]
            [clojure.java.io :as io]
            [clojure.tools.reader :as clj-reader]))

(defn get-js-filename [path-to-manifest]
  (when-let [res (io/resource path-to-manifest)]
    (-> res
        (load-with clj-reader/read)
        first
        :output-name)))

(defn app-js-filename
  [f]
  (fn [resources]
    (let [{:keys [path-to-manifest-js js-asset-path] :as cfg-map} (f resources)]
      (if-let [main-js (get-js-filename (or path-to-manifest-js
                                            "public/js/manifest.edn"))]
        (let [js-path (str (or js-asset-path "/js/") main-js)]
          (-> cfg-map
              (assoc :compiled-js-path js-path)))
        cfg-map))))
