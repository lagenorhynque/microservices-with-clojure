(ns helping-hands.service
  (:require [cheshire.core :as cheshire]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor.chain :as chain]
            [ring.util.response :as ring-resp]))

(defn- get-uid
  "TODO: Integrate with Auth service"
  [token]
  (when (and (string? token)
             (not (empty? token)))
    ;; validate token
    {"uid" "hhuser"}))

(def auth
  {:name ::auth
   :enter
   (fn [context]
     (let [token (-> context :request :headers (get "token"))]
       (if-let [uid (and (not (nil? token))
                         (get-uid token))]
         (assoc-in context [:request :tx-data :user] uid)
         (chain/terminate
          (assoc context
                 :response {:status 401
                            :body "Auth token not found"})))))
   :error
   (fn [context ex-info]
     (assoc context
            :response {:status 500
                       :body (.getMessage ex-info)}))})

(defn- get-service-details
  "TODO: Get the service details from external API"
  [sid]
  {"sid" sid, "name" "House Cleaning"})

(def data-validate
  {:name ::validate
   :enter
   (fn [context]
     (let [sid (-> context :request :form-params :sid)]
       (if-let [service (and (not (nil? sid))
                             (get-service-details sid))]
         (assoc-in context [:request :tx-data :service] service)
         (chain/terminate
          (assoc context
                 :response {:status 400
                            :body "Invalid Service ID"})))))
   :error
   (fn [context ex-info]
     (assoc context
            :response {:status 500
                       :body (.getMessage ex-info)}))})

(defn home-page
  [request]
  (ring-resp/response
   (if-let [uid (-> request :tx-data :user (get "uid"))]
     (cheshire/generate-string {:msg (str "Hello " uid "!")})
     (cheshire/generate-string {:msg "Hello World!"}))))

;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(def common-interceptors [(body-params/body-params) http/html-body])

;; Tabular routes
(def routes #{["/" :post (conj common-interceptors `auth `data-validate `home-page)]})

;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])


;; Consumed by helping-hands.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::http/type :jetty
              ;;::http/host "localhost"
              ::http/port 8080
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false}})
