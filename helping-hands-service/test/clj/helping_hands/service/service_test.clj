(ns helping-hands.service.service-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :as t]
            [helping-hands.service.service :as service]
            [io.pedestal.http :as http]
            [io.pedestal.test :as pt]))

(def service
  (::http/service-fn (http/create-servlet service/service)))

(t/deftest service-upsert-test
  (let [res (pt/response-for service :put "/services/1"
                             :headers {"token" "123"
                                       "Content-Type" "application/x-www-form-urlencoded"}
                             :body "type=A&provider=1&area=bangalore&cost=250")]
    (t/is (= 200
             (:status res)))
    (t/is (= {"service/id" "1"
              "service/type" "A"
              "service/provider" "1"
              "service/area" ["bangalore"]
              "service/cost" 250.0}
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :get "/services/1"
                             :headers {"token" "123"})]
    (t/is (= 200
             (:status res)))
    (t/is (= {"service/id" "1"
              "service/type" "A"
              "service/provider" "1"
              "service/area" ["bangalore"]
              "service/cost" 250.0}
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :delete "/services/1"
                             :headers {"token" "123"})]
    (t/is (= 200
             (:status res)))
    (t/is (= "Success"
             (:body res))))
  (let [res (pt/response-for service :get "/services/1"
                             :headers {"token" "123"})]
    (t/is (= 404
             (:status res)))
    (t/is (= "No such service"
             (:body res)))))
