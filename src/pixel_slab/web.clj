(ns pixel-slab.web
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [org.httpkit.server :as http]))

(defonce ^:private server* (atom nil))

(defn- slurp-resource [path]
  (when-let [res (io/resource path)]
    (slurp res)))

(defn- render-template [template vars]
  ;; super simple {{key}} replacement
  (reduce-kv
   (fn [s k v]
     (str/replace s (str "{{" (name k) "}}") (str v)))
   template
   vars))

(defn- content-type [path]
  (cond
    (str/ends-with? path ".css") "text/css; charset=utf-8"
    (str/ends-with? path ".html") "text/html; charset=utf-8"
    :else "application/octet-stream"))

(defn- public-handler
  [{:keys [node role ip nrepl-port http-port refresh-seconds]
    :or {node "slab-01"
         role "pixel-slab"
         ip "127.0.0.1"
         nrepl-port 7888
         http-port 8080
         refresh-seconds 10}}]
  (let [index-tpl (slurp-resource "public/index.html")]
    (fn [req]
      (let [uri (:uri req)]
        (cond
          (= uri "/")
          {:status 200
           :headers {"Content-Type" "text/html; charset=utf-8"}
           :body (render-template index-tpl
                                  {:node node
                                   :role role
                                   :ip ip
                                   :nrepl_port nrepl-port
                                   :http_port http-port
                                   :refresh_seconds refresh-seconds})}

          ;; serve /public/* assets
          (str/starts-with? uri "/")
          (let [path (subs uri 1) ; drop leading /
                res  (io/resource (str "public/" (str/replace path #"^public/" "")))]
            (if res
              {:status 200
               :headers {"Content-Type" (content-type uri)}
               :body (io/input-stream res)}
              {:status 404 :body "not found"}))

          :else
          {:status 404 :body "not found"})))))

(defn running? [] (some? @server*))

(defn stop!
  []
  (when-let [stop-fn @server*]
    (stop-fn)
    (reset! server* nil)
    :stopped))

(defn start!
  [{:keys [ip port node role nrepl-port refresh-seconds]
    :or {ip "127.0.0.1"
         port 8080
         node "slab-01"
         role "pixel-slab"
         nrepl-port 7888
         refresh-seconds 10}}]
  (when (running?) (stop!))
  (let [handler (public-handler {:node node
                                 :role role
                                 :ip ip
                                 :nrepl-port nrepl-port
                                 :http-port port
                                 :refresh-seconds refresh-seconds})
        stop-fn (http/run-server handler {:ip ip :port port})]
    (reset! server* stop-fn)
    {:status :started :ip ip :port port}))
