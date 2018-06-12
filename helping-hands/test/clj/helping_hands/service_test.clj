(ns helping-hands.service-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :as t]
            [helping-hands.service :as service]
            [io.pedestal.http :as http]
            [io.pedestal.test :as pt]))

(def service
  (::http/service-fn (http/create-servlet service/service)))

(t/deftest home-page-test
  (let [res (pt/response-for service :post "/")]
    (t/is (= 401
             (:status res)))
    (t/is (= "Auth token not found"
             (:body res))))
  (let [res (pt/response-for service :post "/"
                             :headers {"token" "1234"})]
    (t/is (= 400
             (:status res)))
    (t/is (= "Invalid Service ID"
             (:body res))))
  (let [res (pt/response-for service :post "/"
                             :headers {"token" "1234"
                                       "Content-Type" "application/x-www-form-urlencoded"}
                             :body "sid=1")]
    (t/is (= 200
             (:status res)))
    (t/is (= {"msg" "Hello hhuser!"}
             (-> res :body cheshire/decode)))))
