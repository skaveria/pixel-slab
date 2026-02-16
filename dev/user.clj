(ns user
  (:require
   [clojure.java.shell :as sh]
   [clojure.tools.namespace.repl :refer [refresh]]
   [pixel-slab.core :as slab]))

(defn go []
  (slab/start!))

(defn stop []
  (slab/stop!))

(defn say
  ([text] (slab/say! text))
  ([opts text] (slab/say! opts text)))

(defn git-pull []
  (println "Pulling latest changes...")
  (let [{:keys [exit out err]} (sh/sh "git" "pull")]
    (when (seq out) (println out))
    (when (seq err) (println err))
    (if (zero? exit)
      (println "Pull complete.")
      (println "Git pull failed.")))
  :done)

(defn reload []
  (println "Reloading namespaces...")
  (refresh))

(defn slab-update []
  (git-pull)
  (reload)
  :updated)
