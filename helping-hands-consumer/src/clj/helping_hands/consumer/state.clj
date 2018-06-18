(ns helping-hands.consumer.state
  (:require [helping-hands.consumer.persistence :as persistence]
            [mount.core :refer [defstate]]))

(defstate consumerdb
  :start (persistence/create-consumer-database))
