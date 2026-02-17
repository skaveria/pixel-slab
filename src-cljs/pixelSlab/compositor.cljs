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

;; ------------------------------------------------------------
;; Chrome manifest (single source of truth)
;; ------------------------------------------------------------

(def node-name "slab-01")

(def tabs
  [{:href "index.html"  :label "dashboard"}
   {:href "system.html" :label "system"}
   {:href "git.html"    :label "git"}
   {:href "event.html"  :label "event"}
   {:href "notes.html"  :label "notes"}
   {:href "about.html"  :label "about"}
   {:href "media.html"  :label "media"}])

(defn- file-stem [filename]
  (-> filename
      (str/replace #"\.html$" "")
      (str/replace #"\.htm$"  "")))

(defn- current-page []
  ;; Works for both http:// and file:// previews
  (let [proto (.-protocol js/location)
        p (.-pathname js/location)
        base (if (or (nil? p) (= p "/")) "index.html" (basename p))]
    (if (= proto "file:") (basename p) base)))

(defn- page-label [page]
  (or (:label (first (filter #(= (:href %) page) tabs)))
      (file-stem page)))

;; ------------------------------------------------------------
;; DOM helpers
;; ------------------------------------------------------------

(defn- append! [parent child]
  (.appendChild parent child)
  parent)

(defn- mk-span [cls txt]
  (let [el (.createElement js/document "span")]
    (set! (.-className el) cls)
    (set! (.-textContent el) (str txt))
    el))

(defn- clear-children! [el]
  (set! (.-innerHTML el) "")
  el)

(defn- state-val [state k fallback]
  (let [v (when state (aget state k))]
    (if (or (nil? v) (undefined? v) (= "" (str v)))
      fallback
      (str v))))

(defn- mk-seg
  "Create: <span class='seg'><span class='k'>..</span><span class='v ..'>..</span></span>"
  [k v]
  (let [seg (.createElement js/document "span")
        k-el (mk-span "k" k)
        v-el (mk-span "v" v)
        klass (classify-value v)]
    (set! (.-className seg) "seg")
    (when (seq klass)
      (.. v-el -classList (add klass)))
    (append! seg k-el)
    (append! seg v-el)
    seg))

(defn- by-id [id] (.getElementById js/document id))

(defn- qs [sel] (.querySelector js/document sel))

(defn- mk-el
  ([tag] (.createElement js/document tag))
  ([tag class-name]
   (let [e (.createElement js/document tag)]
     (set! (.-className e) class-name)
     e)))

(defn- set-text! [el s]
  (set! (.-textContent el) (str s))
  el)

(defn- append! [parent child]
  (.appendChild parent child)
  parent)

(defn- insert-after! [new-node ref-node]
  (when-let [p (.-parentNode ref-node)]
    (if-let [n (.-nextSibling ref-node)]
      (.insertBefore p new-node n)
      (.appendChild p new-node)))
  new-node)

(defn- ensure-content-root! []
  ;; Org export gives you #content; if not, we make one and wrap body children.
  (or (by-id "content")
      (let [content (mk-el "div" "content")
            body (.-body js/document)
            kids (array-seq (.-childNodes body))]
        (set! (.-id content) "content")
        (doseq [k kids]
          ;; Move into content (avoid moving script tags if any are at end; but safe for your pages)
          (.appendChild content k))
        (.appendChild body content)
        content)))

;; ------------------------------------------------------------
;; Active tab highlight
;; ------------------------------------------------------------
(defn highlight-active-tab! []
  (when-let [bar (qs "#content > p.slab-tabs")]
    (let [current (current-page)
          links (array-seq (.querySelectorAll bar "a"))]
      (doseq [a links] (.. a -classList (remove "active")))
      (when-let [match (first (filter (fn [a]
                                        (= (basename (.getAttribute a "href")) current))
                                      links))]
        (.. match -classList (add "active"))))))

;; ------------------------------------------------------------
;; Modeline (structured segments)
;; ------------------------------------------------------------

(defn- ensure-modeline! []
  (when-let [post (.getElementById js/document "postamble")]
    (let [existing (.getElementById js/document "modeline")
          modeline (or existing
                       (let [d (.createElement js/document "div")]
                         (set! (.-id d) "modeline")
                         (.appendChild post d)
                         d))]
      modeline)))

(defn render-modeline! [state]
  (when-let [m (ensure-modeline!)]
    (clear-children! m)
    (let [nrepl   (state-val state "nrepl" "—")
          http    (state-val state "http" "—")
          time    (state-val state "time" "—")
          battery (state-val state "battery" "—")
          volume  (state-val state "volume" "—")
          gitrev  (state-val state "git_rev" "—")
          sep-el  (fn [] (mk-span "sep" "·"))]

      (append! m (mk-span "node" "pixel-slab"))

      (doseq [seg [(sep-el) (mk-seg ":nrepl" nrepl)
                   (sep-el) (mk-seg ":http" http)
                   (sep-el) (mk-seg ":time" time)
                   (sep-el) (mk-seg ":battery" battery)
                   (sep-el) (mk-seg ":volume" volume)
                   (sep-el) (mk-seg ":git" gitrev)]]
        (append! m seg))))
  nil)

;; ------------------------------------------------------------
;; Pane transform (unchanged from your working version)
;; ------------------------------------------------------------

(defn mount-pane! [pre]
  (when-not (= "1" (.getAttribute pre "data-slab-mounted"))
    (.setAttribute pre "data-slab-mounted" "1")

    (let [raw (-> (.-textContent pre) (str/replace #"\r\n" "\n") (str/trimr))
          lines (->> (str/split raw #"\n") (map str/trimr) (into []))
          [pane-type lines] (if (and (seq lines) (str/starts-with? (first lines) "@"))
                              [(-> (subs (first lines) 1) str/trim str/lower-case)
                               (subvec lines 1)]
                              [nil lines])
          _ (when pane-type
              (.. pre -classList (add (str "pane-" pane-type))))
          non-empty (->> lines (filter #(seq (str/trim %))) (into []))
          kv-pairs (->> non-empty (map split-kv) (filter some?) (into []))
          is-kv (>= (count kv-pairs) (max 1 (js/Math.floor (* 0.7 (count non-empty)))))]

      (if-not is-kv
        (do
          (.setAttribute pre "data-pane-kind" "text")
          (.setAttribute pre "data-template" (str/join "\n" lines)))
        (do
          (.setAttribute pre "data-pane-kind" "kv")
          (.. pre -classList (add "pane-kv"))

          (let [kv (.createElement js/document "div")]
            (set! (.-className kv) "kv")

            (doseq [[k v] kv-pairs]
              (let [row (.createElement js/document "div")
                    kdiv (.createElement js/document "div")
                    vdiv (.createElement js/document "div")]
                (set! (.-className row) "row")
                (set! (.-className kdiv) "k")
                (set! (.-textContent kdiv) k)

                (set! (.-className vdiv) "v")
                (.setAttribute vdiv "data-template" v)
                (set! (.-textContent vdiv) v)

                (.appendChild row kdiv)
                (.appendChild row vdiv)
                (.appendChild kv row)))

            (set! (.-textContent pre) "")
            (.appendChild pre kv)))))))

(defn mount-all-panes! []
  (doseq [pre (array-seq (.querySelectorAll js/document "pre.example"))]
    (mount-pane! pre)))

(defn render-panes! [state]
  (doseq [pre (array-seq (.querySelectorAll js/document "pre.example"))]
    (let [kind (.getAttribute pre "data-pane-kind")]
      (cond
        (= kind "text")
        (let [tmpl (.getAttribute pre "data-template")]
          (set! (.-textContent pre) (apply-template tmpl state)))

        (= kind "kv")
        (doseq [vdiv (array-seq (.querySelectorAll pre ".v"))]
          (let [tmpl (.getAttribute vdiv "data-template")
                rendered (apply-template tmpl state)
                klass (classify-value rendered)]
            (set! (.-textContent vdiv) rendered)
            (.. vdiv -classList (remove "warn"))
            (.. vdiv -classList (remove "bad"))
            (when (seq klass)
              (.. vdiv -classList (add klass)))))))))

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
  (ensure-chrome!)
  (set! (.. js/document -documentElement -dataset -slab) "1")
  (highlight-active-tab!)
  (mount-all-panes!)
  (-> (fetch-state!)
      (.then (fn [state]
               (if state
                 (do
                   (render-panes! state)
                   (render-modeline! state))
                 (render-modeline! (clj->js {})))))))

(defn init []
  (tick!)
  (js/setInterval tick! poll-ms))

(defn- ensure-titlebar! []
  (let [content (ensure-content-root!)
        existing (qs "#content > h1.title")
        page (current-page)
        title (str node-name " · " (page-label page))]
    (or existing
        (let [h (mk-el "h1" "title")]
          (set-text! h title)
          ;; IMPORTANT: use firstElementChild, not firstChild (skip whitespace)
          (if-let [first-el (.-firstElementChild content)]
            (.insertBefore content h first-el)
            (.appendChild content h))
          h))))

(defn- ensure-tabsbar! []
  (let [content (ensure-content-root!)
        existing (qs "#content > p.slab-tabs")]
    (or existing
        (let [p (mk-el "p" "slab-tabs")
              title (or (qs "#content > h1.title") (ensure-titlebar!))]
          ;; build links
          (doseq [{:keys [href label]} tabs]
            (let [a (mk-el "a")]
              (.setAttribute a "href" href)
              (set-text! a label)
              (append! p a)))

          ;; IMPORTANT: insert after the title using element-sibling logic
          (if-let [next-el (.-nextElementSibling title)]
            (.insertBefore content p next-el)
            (.appendChild content p))

          p))))

(defn- ensure-postamble! []
  ;; Org gives #postamble when html-postamble:t; if missing, we create one.
  (or (by-id "postamble")
      (let [p (mk-el "div" "status")]
        (set! (.-id p) "postamble")
        (.appendChild (.-body js/document) p)
        p)))

(defn- ensure-modeline-el! []
  (let [post (ensure-postamble!)
        existing (by-id "modeline")]
    (or existing
        (let [m (mk-el "div")]
          (set! (.-id m) "modeline")
          ;; default template string (your renderer will override with spans later)
          (.setAttribute m "data-template" "pixel-slab · :nrepl {{nrepl}} · :http {{http}} · :time {{time}} · :battery {{battery}} · :volume {{volume}}")
          (append! post m)
          m))))

(defn ensure-chrome! []
  ;; Safe to call every tick (idempotent).
  (ensure-titlebar!)
  (ensure-tabsbar!)
  (ensure-modeline-el!)
  nil)
