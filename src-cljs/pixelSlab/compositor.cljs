(ns pixelSlab.compositor
  (:require [clojure.string :as str]))

(def poll-ms 2500)

(def token-test (re-pattern "\\{\\{[a-zA-Z0-9_]+\\}\\}"))
(def token-repl (re-pattern "\\{\\{([a-zA-Z0-9_]+)\\}\\}"))

(defn basename [s]
  (let [s (or s "")
        s (first (str/split s #"[?#]"))
        parts (str/split s #"/")]
    (or (last parts) "index.html")))

(defn current-page-name []
  (if (= "file:" (.-protocol js/location))
    (basename (.-pathname js/location))
    (let [p (.-pathname js/location)]
      (if (or (nil? p) (= p "/")) "index.html" (basename p)))))

(defn apply-template [tmpl state]
  (let [tmpl (str tmpl)]
    (if-not (re-find token-test tmpl)
      tmpl
      (str/replace tmpl token-repl
                   (fn [[_ k]]
                     (let [v (when state (aget state k))]
                       (if (or (nil? v) (undefined? v)) "" (str v))))))))

(defn classify-value [v]
  (let [t (-> (str v) str/lower-case)]
    (cond
      (or (str/includes? t "error")
          (str/includes? t "failed")
          (str/includes? t "offline")
          (str/includes? t "down")) "bad"
      (or (str/includes? t "warn")
          (str/includes? t "charging")
          (str/includes? t "dirty")
          (str/includes? t "throttl")
          (str/includes? t "hot")) "warn"
      :else "")))

(defn split-kv [line]
  (cond
    (str/includes? line "::")
    (let [[k & more] (str/split line #"::")]
      [(str/trim k) (str/trim (str/join "::" more))])

    :else
    (when-let [[_ k v] (re-matches #"^\s*([^\s]+)\s{2,}(.+)$" line)]
      [(str/trim k) (str/trim v)])))

(defn highlight-active-tab! []
  (when-let [bar (.querySelector js/document "h1 + p")]
    (let [current (current-page-name)
          links (array-seq (.querySelectorAll bar "a"))]
      (doseq [a links] (.classList.remove (.-classList a) "active"))
      (when-let [match (first (filter (fn [a]
                                        (= (basename (.getAttribute a "href"))
                                           current))
                                      links))]
        (.classList.add (.-classList match) "active"))
      (when (and (= current "index.html") (seq links)
                 (nil? (first (filter (fn [a]
                                        (= (basename (.getAttribute a "href")) current))
                                      links))))
        (.classList.add (.-classList (first links)) "active")))))

;; ---------------- modeline (structured) ----------------

(defn- ensure-modeline! []
  (when-let [post (.getElementById js/document "postamble")]
    (let [existing (.getElementById js/document "modeline")
          modeline (or existing
                       (let [d (.createElement js/document "div")]
                         (set! (.-id d) "modeline")
                         (.appendChild post d)
                         d))]
      modeline)))

(defn- mk-span [cls text]
  (let [s (.createElement js/document "span")]
    (set! (.-className s) cls)
    (set! (.-textContent s) (str text))
    s))

(defn- state-val
  "Get a state key (string) with fallback. Also supports templated values."
  [state k fallback]
  (let [v (when state (aget state k))]
    (cond
      (or (nil? v) (undefined? v) (= "" (str v))) fallback
      :else (str v))))

(defn render-modeline!
  "Render a WM-style bar: key/value segments with a dot separator."
  [state]
  (when-let [m (ensure-modeline!)]
    ;; Clear existing children
    (set! (.-innerHTML m) "")

    ;; Values (fallbacks are visible but calm)
    (let [nrepl   (state-val state "nrepl"   "—")
          http    (state-val state "http"    "—")
          time    (state-val state "time"    "—")
          battery (state-val state "battery" "—")
          volume  (state-val state "volume"  "—")]

      ;; Node label (no key)
      (.appendChild m (mk-span "node" "pixel-slab"))

      ;; Separator helper
      (let [sep (fn [] (.appendChild m (mk-span "sep" "·")))]
        (sep)

        ;; segment helper: :key value
        (let [seg (fn [k v]
                    (let [seg-el (.createElement js/document "span")
                          _ (set! (.-className seg-el) "seg")
                          k-el (mk-span "k" k)
                          v-el (mk-span "v" v)
                          klass (classify-value v)]
                      (when (seq klass)
                        (.classList.add (.-classList v-el) klass))
                      (.appendChild seg-el k-el)
                      (.appendChild seg-el v-el)
                      (.appendChild m seg-el)))]

          (seg ":nrepl" nrepl)   (sep)
          (seg ":http"  http)    (sep)
          (seg ":time"  time)    (sep)
          (seg ":battery" battery) (sep)
          (seg ":volume" volume))))))

;; ---------------- panes (kept minimal here) ----------------
;; You can keep your existing pane mount/render code; modeline is independent.

(defn dev-state-override []
  (let [x (aget js/window "SLAB_STATE")]
    (when (and x (not (undefined? x)) (= "object" (js/typeof x))) x)))

(defn fetch-state! []
  (cond
    (dev-state-override)
    (js/Promise.resolve (dev-state-override))

    (= "file:" (.-protocol js/location))
    (js/Promise.resolve nil)

    :else
    (-> (js/fetch "/api/state" (clj->js {:cache "no-store"}))
        (.then (fn [res] (if (.-ok res) (.json res) nil)))
        (.catch (fn [_] nil)))))

(defn tick! []
  (highlight-active-tab!)
  (-> (fetch-state!)
      (.then (fn [state]
               (render-modeline! state)))))

(defn init []
  (tick!)
  (js/setInterval tick! poll-ms))
