(ns pedestal-play.datomic
  (:require [datomic.api :as d]))

(comment

  (def dburi "datomic:mem://hhorder")

  (d/create-database dburi)

  (def conn (d/connect dburi))

  (def result
    (d/transact conn
                [{:db/ident :order/name
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db/doc "Display Name of Order"
                  :db/index true}
                 {:db/ident :order/status
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db/doc "Order Status"}
                 {:db/ident :order/rating
                  :db/valueType :db.type/long
                  :db/cardinality :db.cardinality/one
                  :db/doc "Rating for the Order"}
                 {:db/ident :order/contact
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db/doc "Contact Email Address"}]))

  (def order-result
    (d/transact conn
                [{:db/id 1
                  :order/name "Cleaning Order"
                  :order/status "Done"
                  :order/rating 5
                  :order/contact "abc@hh.com"}
                 {:db/id 2
                  :order/name "Gardening Order"
                  :order/status "Pending"
                  :order/rating 4
                  :order/contact "def@hh.com"}]))

  (d/q '[:find ?e ?n ?c ?s
         :where
         [?e :order/rating 5]
         [?e :order/name ?n]
         [?e :order/contact ?c]
         [?e :order/status ?s]]
       (d/db conn))

  ;; returns only the entity ID of the entities matching the clause
  (d/q '[:find ?e
         :where [?e :order/rating 5]]
       (d/db conn))

  ;; find all the entities with the three attributes and entity ID
  (d/q '[:find ?e ?n ?c ?s
         :where
         [?e :order/name ?n]
         [?e :order/contact ?c]
         [?e :order/status ?s]]
       (d/db conn))

  ;; using 'or' clause
  (d/q '[:find ?e ?n ?c ?s
         :where
         (or [?e :order/rating 4] [?e :order/rating 5])
         [?e :order/name ?n]
         [?e :order/contact ?c]
         [?e :order/status ?s]]
       (d/db conn))

  ;; using predicate
  (d/q '[:find ?e ?n ?c ?s
         :where
         [?e :order/rating ?r]
         [?e :order/name ?n]
         [?e :order/contact ?c]
         [?e :order/status ?s]
         [(< ?r 5)]]
       (d/db conn))

  (d/q '[:find ?e ?s
         :where [?e :order/status ?s]]
       (d/db conn))

  ;; update the status attribute to 'Done' for order ID '2'
  (def status-result
    (d/transact conn
                [{:db/id 2 :order/status "Done"}]))

  ;; query the latest state of database
  (d/q '[:find ?e ?s
         :where [?e :order/status ?s]]
       (d/db conn))

  ;; query the status on previous state
  (d/q '[:find ?e ?s
         :where [?e :order/status ?s]]
       (@status-result :db-before))

  (d/delete-database dburi)

  )
