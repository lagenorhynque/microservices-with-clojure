(ns helping-hands.order.service-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :as t]
            [helping-hands.order.service :as service]
            [io.pedestal.http :as http]
            [io.pedestal.test :as pt]))

(def service
  (::http/service-fn (http/create-servlet service/service)))

(t/deftest order-upsert-test
  (let [res (pt/response-for service :put "/orders/1"
                             :headers {"token" "1"
                                       "Content-Type" "application/x-www-form-urlencoded"}
                             :body "service=1&provider=1&consumer=1&cost=500&status=O")]
    (t/is (= 200
             (:status res)))
    (t/is (= {"order/id" "1"
              "order/service" "1"
              "order/provider" "1"
              "order/consumer" "1"
              "order/cost" 500.0
              "order/status" "O"}
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :put "/orders/2"
                             :headers {"token" "1"
                                       "Content-Type" "application/x-www-form-urlencoded"}
                             :body "service=2&provider=2&consumer=1&cost=250&status=O")]
    (t/is (= 200
             (:status res)))
    (t/is (= {"order/id" "2"
              "order/service" "2"
              "order/provider" "2"
              "order/consumer" "1"
              "order/cost" 250.0
              "order/status" "O"}
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :put "/orders/3"
                             :headers {"token" "2"
                                       "Content-Type" "application/x-www-form-urlencoded"}
                             :body "service=1&provider=1&consumer=2&cost=250&status=I")]
    (t/is (= 200
             (:status res)))
    (t/is (= {"order/id" "3"
              "order/service" "1"
              "order/provider" "1"
              "order/consumer" "2"
              "order/cost" 250.0
              "order/status" "I"}
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :get "/orders"
                             :headers {"token" "1"})]
    (t/is (= 200
             (:status res)))
    (t/is (= [{"order/id" "1"
               "order/service" "1"
               "order/provider" "1"
               "order/consumer" "1"
               "order/cost" 500.0
               "order/status" "O"}
              {"order/id" "2"
               "order/service" "2"
               "order/provider" "2"
               "order/consumer" "1"
               "order/cost" 250.0
               "order/status" "O"}]
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :get "/orders"
                             :headers {"token" "2"})]
    (t/is (= 200
             (:status res)))
    (t/is (= [{"order/id" "3"
               "order/service" "1"
               "order/provider" "1"
               "order/consumer" "2"
               "order/cost" 250.0
               "order/status" "I"}]
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :get "/orders?flds=order/service,order/status"
                             :headers {"token" "1"})]
    (t/is (= 200
             (:status res)))
    (t/is (= [{"order/service" "1"
               "order/status" "O"}
              {"order/service" "2"
               "order/status" "O"}]
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :delete "/orders/2"
                             :headers {"token" "1"})]
    (t/is (= 200
             (:status res)))
    (t/is (= "Success"
             (:body res))))
  (let [res (pt/response-for service :get "/orders/2"
                             :headers {"token" "1"})]
    (t/is (= 404
             (:status res)))
    (t/is (= "No such order"
             (:body res))))
  (let [res (pt/response-for service :get "/orders?flds=order/service,order/status"
                             :headers {"token" "1"})]
    (t/is (= 200
             (:status res)))
    (t/is (= [{"order/service" "1"
               "order/status" "O"}]
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :delete "/orders/3"
                             :headers {"token" "1"})]
    (t/is (= 200
             (:status res)))
    (t/is (= "Success"
             (:body res))))
  (let [res (pt/response-for service :get "/orders"
                             :headers {"token" "2"})]
    (t/is (= 404
             (:status res)))
    (t/is (= "No such orders"
             (:body res)))))
