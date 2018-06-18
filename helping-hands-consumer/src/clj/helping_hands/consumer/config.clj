(ns helping-hands.consumer.config
  "Defines Configuration for the Service"
  (:require [omniconf.core :as cfg]))

(defn int-config
  "Initializes the configuration"
  [{:keys [cli-args] :as params
    :or {cli-args []}}]
  ;; define the configuration
  (cfg/define {:conf {:type :file
                      :required true
                      :verifier cfg/verify-file-exists
                      :description "MECBOT configuration file"}
               :datomic {:nested
                         {:uri {:type :string
                                :default "datomic:mem://consumer"
                                :description "Datomic URI for Consumer Database"}}}})
  ;; like- :some-option => SOME_OPTION
  (cfg/populate-from-env)
  ;; load properties to pick -Dconf for the config file
  (cfg/populate-from-properties)
  ;; Configuration file specified as
  ;; Environment variable CONF or JVM opt -Dconf
  (when-let [conf (cfg/get :conf)]
    (cfg/populate-from-file conf))
  ;; like- :some-option => (java -Dsome-option=...)
  ;; reload JVM args to overwrite configuration file params
  (cfg/populate-from-properties)
  ;; like- :some-option => -some-option
  (cfg/populate-from-cmd cli-args)
  ;; verify the configuration
  (cfg/verify))

(defn get-config
  "Gets the specified config param value"
  [& args]
  (apply cfg/get args))
