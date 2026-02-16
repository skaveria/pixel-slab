(ns user
  (:require [pixel-slab.core :as slab]))

(defn go [] (slab/start!))
(defn stop [] (slab/stop!))
(defn say
  ([text] (slab/say! text))
  ([opts text] (slab/say! opts text)))
