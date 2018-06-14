(ns helping-hands.provider.service-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :as t]
            [helping-hands.provider.service :as service]
            [io.pedestal.http :as http]
            [io.pedestal.test :as pt]))

(def service
  (::http/service-fn (http/create-servlet service/service)))

(t/deftest provider-upsert-test
  (let [res (pt/response-for service :put "/providers/1"
                             :headers {"token" "123"
                                       "Content-Type" "application/x-www-form-urlencoded"}
                             :body "name=ProviderA")]
    (t/is (= 200
             (:status res)))
    (t/is (= {"provider/id" "1"
              "provider/name" "ProviderA"}
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :put "/providers/1/rate"
                             :headers {"token" "123"
                                       "Content-Type" "application/x-www-form-urlencoded"}
                             :body "rating=5.0")]
    (t/is (= 200
             (:status res)))
    (t/is (= {"provider/id" "1"
              "provider/name" "ProviderA"
              "provider/rating" [5.0]}
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :get "/providers/1"
                             :headers {"token" "123"})]
    (t/is (= 200
             (:status res)))
    (t/is (= {"provider/id" "1"
              "provider/name" "ProviderA"
              "provider/rating" [5.0]}
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :get "/providers/2"
                             :headers {"token" "123"})]
    (t/is (= 404
             (:status res)))
    (t/is (= "No such provider"
             (:body res))))
  (let [res (pt/response-for service :delete "/providers/1"
                             :headers {"token" "123"})]
    (t/is (= 200
             (:status res)))
    (t/is (= "Success"
             (:body res))))
  (let [res (pt/response-for service :get "/providers/1"
                             :headers {"token" "123"})]
    (t/is (= 404
             (:status res)))
    (t/is (= "No such provider"
             (:body res)))))
