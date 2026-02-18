(ns pixel-slab.web
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [org.httpkit.server :as http]
            [pixel-slab.state :as state]))

(defonce ^:private server* (atom nil))

(defn running? [] (some? @server*))

(defn stop!
  []
  (when-let [stop-fn @server*]
    (stop-fn)
    (reset! server* nil)
    :stopped))

(defn- content-type [uri]
  (cond
    (str/ends-with? uri ".css")  "text/css; charset=utf-8"
    (str/ends-with? uri ".js")   "application/javascript; charset=utf-8"
    (str/ends-with? uri ".html") "text/html; charset=utf-8"
    (str/ends-with? uri ".json") "application/json; charset=utf-8"
    (str/ends-with? uri ".edn")  "application/edn; charset=utf-8"
    (str/ends-with? uri ".png")  "image/png"
    (str/ends-with? uri ".jpg")  "image/jpeg"
    (str/ends-with? uri ".jpeg") "image/jpeg"
    (str/ends-with? uri ".gif")  "image/gif"
    (str/ends-with? uri ".mp4")  "video/mp4"
    :else                        "application/octet-stream"))

(def ^:private public-dir
  ;; Serve from filesystem first so shadow-cljs outputs are visible immediately.
  (io/file "resources" "public"))

(defn- file-response [uri]
  (let [path (subs uri 1)
        f (io/file public-dir path)]
    (when (and (.exists f) (.isFile f))
      {:status 200
       :headers {"Content-Type" (content-type uri)
                 ;; helps a lot during dev; you can relax later
                 "Cache-Control" "no-store"}
       :body (io/input-stream f)})))

(defn- resource-response [uri]
  ;; Fallback: classpath resource (useful if you ever package this)
  (let [resource-path (str "public/" (subs uri 1))]
    (if-let [res (io/resource resource-path)]
      {:status 200
       :headers {"Content-Type" (content-type uri)
                 "Cache-Control" "no-store"}
       :body (io/input-stream res)}
      {:status 404
       :headers {"Content-Type" "text/plain; charset=utf-8"}
       :body (str "not found: " uri)})))

(defn- static-response [uri]
  (or (file-response uri)
      (resource-response uri)))

(defn- json-escape [s]
  (-> (str s)
      (str/replace "\\" "\\\\")
      (str/replace "\"" "\\\"")
      (str/replace "\n" "\\n")
      (str/replace "\r" "\\r")
      (str/replace "\t" "\\t")))

(defn- map->json [m]
  ;; minimal JSON encoder (string values)
  (str "{"
       (str/join
        ","
        (for [[k v] m]
          (str "\"" (name k) "\":\"" (json-escape v) "\"")))
       "}"))

(defn- handler []
  (fn [req]
    (let [uri (:uri req)]
      (cond
        ;; API
        (= uri "/api/state")
        (let [m (state/snapshot)]
          {:status 200
           :headers {"Content-Type" "application/json; charset=utf-8"
                     "Cache-Control" "no-store"}
           :body (map->json m)})

        ;; Root -> index.html
        (= uri "/")
        (static-response "/index.html")

        ;; Any static file under resources/public (filesystem first)
        (re-matches #"/[A-Za-z0-9._/-]+" uri)
        (static-response uri)

        :else
        {:status 404
         :headers {"Content-Type" "text/plain; charset=utf-8"}
         :body "not found"}))))

(defn start!
  [{:keys [ip port]
    ;; IMPORTANT: 0.0.0.0 allows phone/LAN access
    :or {ip "0.0.0.0" port 8080}}]
  (when (running?) (stop!))
  (let [stop-fn (http/run-server (handler) {:ip ip :port port})]
    (reset! server* stop-fn)
    {:status :started :ip ip :port port}))
