(defproject helping-hands-consumer "0.0.1-SNAPSHOT"
  :description "Helping Hands Consumer Application"
  :url "https://www.packtpub.com/application-development/microservices-clojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [io.pedestal/pedestal.service "0.5.3"]
                 [io.pedestal/pedestal.jetty "0.5.3"]
                 ;; Datomic Free Edition
                 [com.datomic/datomic-free "0.9.5697"]
                 ;; Omniconf
                 [com.grammarly/omniconf "0.3.1"]
                 ;; Mount
                 [mount "0.1.12"]
                 [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.25"]
                 [org.slf4j/jcl-over-slf4j "1.7.25"]
                 [org.slf4j/log4j-over-slf4j "1.7.25"]]
  :min-lein-version "2.0.0"
  :source-paths ["src/clj"]
  :java-source-paths ["src/jvm"]
  :test-paths ["test/clj" "test/jvm"]
  :resource-paths ["config", "resources"]
  :plugins [[lein-codox "0.10.3"]
            ;; code coverage
            [lein-cloverage "1.0.10"]
            ;; unit test docs
            [test2junit "1.4.2"]]
  :codox {:namespaces :all}
  :test2junit-output-dir "target/test-reports"
  :profiles {:provided {:dependencies [[org.clojure/tools.reader "1.2.2"]
                                       [org.clojure/tools.nrepl "0.2.13"]]}
             :dev {:aliases {"run-dev" ["trampoline" "run" "-m" "helping-hands.consumer.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.5.3"]]
                   :resource-paths ["config" "resources"]
                   :jvm-opts ["-Dconf=config/conf.edn"]}
             :uberjar {:aot [helping-hands.consumer.server]}
             :doc {:dependencies [[codox-theme-rdash "0.1.2"]]
                   :codox {:metadata {:doc/format :markdown}
                           :theme [:rdash]}}
             :debug {:jvm-opts
                     ["-server" (str "-agentlib:jdwp=transport=dt_socket,"
                                     "server=y,address=8000,suspend=n")]}}
  :main ^{:skip-aot true} helping-hands.consumer.server)
