(ns helping-hands.consumer.service-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :as t]
            [helping-hands.consumer.service :as service]
            [io.pedestal.http :as http]
            [io.pedestal.test :as pt]))

(def service
  (::http/service-fn (http/create-servlet service/service)))

(t/deftest consumer-upsert-test
  (let [res (pt/response-for service :put "/consumers/1"
                             :headers {"token" "123"
                                       "Content-Type" "application/x-www-form-urlencoded"}
                             :body "name=ConsumerA")]
    (t/is (= 200
             (:status res)))
    (t/is (= {"consumer/id" "1"
              "consumer/name" "ConsumerA"}
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :put "/consumers/1"
                             :headers {"token" "123"
                                       "Content-Type" "application/x-www-form-urlencoded"}
                             :body "email=user1@helpinghands.com")]
    (t/is (= 200
             (:status res)))
    (t/is (= {"consumer/id" "1"
              "consumer/name" "ConsumerA"
              "consumer/email" "user1@helpinghands.com"}
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :get "/consumers/1"
                             :headers {"token" "123"})]
    (t/is (= 200
             (:status res)))
    (t/is (= {"consumer/id" "1"
              "consumer/name" "ConsumerA"
              "consumer/email" "user1@helpinghands.com"}
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :get "/consumers/1?flds=consumer/name,consumer/email"
                             :headers {"token" "123"})]
    (t/is (= 200
             (:status res)))
    (t/is (= {"consumer/name" "ConsumerA"
              "consumer/email" "user1@helpinghands.com"}
             (-> res :body cheshire/decode))))
  (let [res (pt/response-for service :get "/consumers/2"
                             :headers {"token" "123"})]
    (t/is (= 404
             (:status res)))
    (t/is (= "No such consumer"
             (:body res))))
  (let [res (pt/response-for service :delete "/consumers/1"
                             :headers {"token" "123"})]
    (t/is (= 200
             (:status res)))
    (t/is (= "Success"
             (:body res)))))

(t/deftest consumer-create-test
  (let [res (pt/response-for service :post "/consumers"
                             :headers {"token" "123"
                                       "Content-Type" "application/x-www-form-urlencoded"}
                             :body "name=ConsumerX&email=userx@helpinghands.com")
        {consumer-id "consumer/id"
         consumer-name "consumer/name"
         consumer-email "consumer/email"
         :as body} (-> res :body cheshire/decode)]
    (t/is (= 200
             (:status res)))
    (t/is (= 3
             (count body)))
    (t/is (= "ConsumerX" consumer-name))
    (t/is (= "userx@helpinghands.com" consumer-email))
    (let [res (pt/response-for service :get (str "/consumers/" consumer-id)
                               :headers {"token" "123"})]
      (t/is (= 200
               (:status res)))
      (t/is (= {"consumer/id" consumer-id
                "consumer/name" consumer-name
                "consumer/email" consumer-email}
               (-> res :body cheshire/decode))))
    (let [res (pt/response-for service :delete (str "/consumers/" consumer-id)
                               :headers {"token" "123"})]
      (t/is (= 200
               (:status res)))
      (t/is (= "Success"
               (:body res))))
    (let [res (pt/response-for service :get (str "/consumers/" consumer-id)
                               :headers {"token" "123"})]
      (t/is (= 404
               (:status res)))
      (t/is (= "No such consumer"
               (:body res))))))
