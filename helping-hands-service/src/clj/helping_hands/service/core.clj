(ns helping-hands.service.core
  "Initializes Helping Hands Service Service"
  (:require [cheshire.core :as cheshire]
            [clojure.string :as str]
            [helping-hands.service.persistence :as persistence]
            [io.pedestal.interceptor.chain :as chain])
  (:import (java.io IOException)
           (java.util UUID)))

;; delay the check for database and connection
;; till the first request to access @servicedb
(def ^:private servicedb
  (delay (persistence/create-service-database "service")))

;;; validation interceptors

(defn- provider-exists?
  "Validates the provider via Provider service"
  [provider]
  ;; TODO: Add integration with Provider service via clj-http
  true)

(defn- validate-rating-cost
  "Validates the rating and cost"
  [context]
  (let [rating (-> context :request :form-params :rating)
        cost (-> context :request :form-params :cost)]
    (try
      (let [context (if (not (nil? rating))
                      (assoc-in context [:request :form-params :rating]
                                (Float/parseFloat rating))
                      context)
            context (if (not (nil? cost))
                      (assoc-in context [:request :form-params :cost]
                                (Float/parseFloat cost))
                      context)]
        context)
      (catch Exception e nil))))

(defn- prepare-valid-context
  "Applies validation logic and returns the resulting context"
  [context]
  (let [params (merge (-> context :request :form-params)
                      (-> context :request :query-params)
                      (-> context :request :path-params))
        ctx (validate-rating-cost context)
        params (when (not (nil? ctx))
                 (assoc params
                        :rating (-> ctx :request :form-params :rating)
                        :cost (-> ctx :request :form-params :cost)))]
    (if (and (not (empty? params))
             (not (nil? ctx))
             (:id params) (:type params) (:provider params)
             (:area params) (:cost params)
             (contains? #{"A" "NA" "D"} (:type params))
             (provider-exists? (:provider params)))
      (let [flds (if-let [fl (:flds params)]
                   (map str/trim (str/split fl #","))
                   (vector))
            params (assoc params :flds flds)]
        (assoc context :tx-data params))
      (chain/terminate
       (assoc context
              :response {:status 400
                         :body (str "ID, type, provider, area and cost is mandatory "
                                    "and rating, cost must be a number with type "
                                    "having one of values A, NA or D")})))))

(def validate-id
  {:name ::validate-id
   :enter
   (fn [context]
     (if-let [id (or (-> context :request :form-params :id)
                     (-> context :request :query-params :id)
                     (-> context :request :path-params :id))]
       ;; validate and return a context with tx-data
       ;; or terminated interceptor chain
       (prepare-valid-context context)
       (chain/terminate
        (assoc context
               :response {:status 400
                          :body "Invalid Service ID"}))))
   :error
   (fn [context ex-info]
     (assoc context
            :response {:status 500
                       :body (.getMessage ex-info)}))})

(def validate-id-get
  {:name ::validat-id-get
   :enter
   (fn [context]
     (if-let [id (or (-> context :request :form-params :id)
                     (-> context :request :query-params :id)
                     (-> context :request :path-params :id))]
       ;; validate and return a context with tx-data
       ;; or terminated interceptor chain
       (let [params (merge (-> context :request :form-params)
                           (-> context :request :query-params)
                           (-> context :request :path-params))]
         (if (and (not (empty? params))
                  (:id params))
           (let [flds (if-let [fl (:flds params)]
                        (map str/trim (str/split fl #","))
                        (vector))
                 params (assoc params :flds flds)]
             (assoc context :tx-data params))
           (chain/terminate
            (assoc context
                   :response {:status 400
                              :body "Invalid Service ID"}))))
       (chain/terminate
        (assoc context
               :response {:status 400
                          :body "Invalid Service ID"}))))
   :error
   (fn [context ex-info]
     (assoc context
            :response {:status 500
                       :body (.getMessage ex-info)}))})

(def validate
  {:name ::validate
   :enter
   (fn [context]
     (if-let [params (-> context :request :form-params)]
       ;; validate and return a context with tx-data
       ;; or terminated interceptor chain
       (prepare-valid-context context)
       (chain/terminate
        (assoc context
               :response {:status 400
                          :body "Invalid parameters"}))))
   :error
   (fn [context ex-info]
     (assoc context
            :response {:status 500
                       :body (.getMessage ex-info)}))})

;;; business logic interceptors

(def get-service
  {:name ::service-get
   :enter
   (fn [context]
     (let [tx-data (:tx-data context)
           entity (persistence/entity @servicedb (:id tx-data) (:flds tx-data))]
       (if (empty? entity)
         (assoc context :response {:status 404 :body "No such service"})
         (assoc context :response {:status 200
                                   :body (cheshire/generate-string entity)}))))
   :error
   (fn [context ex-info]
     (assoc context
            :response {:status 500
                       :body (.getMessage ex-info)}))})

(def upsert-service
  {:name ::service-upsert
   :enter
   (fn [context]
     (let [tx-data (:tx-data context)
           id (:id tx-data)
           db (persistence/upsert @servicedb id
                                  (:type tx-data) (:provider tx-data)
                                  (:area tx-data) (:cost tx-data)
                                  (:rating tx-data) (:status tx-data))]
       (if (nil? @db)
         (throw (IOException. (str "Upsert failed for service: " id)))
         (assoc context
                :response {:status 200
                           :body (cheshire/generate-string
                                  (persistence/entity @servicedb id []))}))))
   :error
   (fn [context ex-info]
     (assoc context
            :response {:status 500
                       :body (.getMessage ex-info)}))})

(def create-service
  {:name ::service-create
   :enter
   (fn [context]
     (let [tx-data (:tx-data context)
           ;; generate a random ID if it is not specified
           id (str (UUID/randomUUID))
           tx-data (if (:id tx-data) tx-data (assoc tx-data :id id))
           db (persistence/upsert @servicedb id
                                  (:type tx-data) (:provider tx-data)
                                  (:area tx-data) (:cost tx-data)
                                  (:rating tx-data) (:status tx-data))]
       (if (nil? @db)
         (throw (IOException. (str "Upsert failed for service: " id)))
         (assoc context
                :response {:status 200
                           :body (cheshire/generate-string
                                  (persistence/entity @servicedb id []))}))))
   :error
   (fn [context ex-info]
     (assoc context
            :response {:status 500
                       :body (.getMessage ex-info)}))})

(def delete-service
  {:name ::service-delete
   :enter
   (fn [context]
     (let [tx-data (:tx-data context)
           db (persistence/delete @servicedb (:id tx-data))]
       (if (nil? @db)
         (assoc context :response {:status 404 :body "No such service"})
         (assoc context
                :response {:status 200
                           :body "Success"}))))
   :error
   (fn [context ex-info]
     (assoc context
            :response {:status 500
                       :body (.getMessage ex-info)}))})
