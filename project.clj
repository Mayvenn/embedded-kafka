(defproject embedded-kafka "0.3.1-SNAPSHOT"
  :description "Helpers to run an embedded Kafka Broker"
  :url "https://github.com/Mayvenn/embedded-kafka"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [commons-io/commons-io "2.4"]
                 [clj-kafka "0.3.2"]]
  :profiles
  {:dev {:source-paths ["dev"]
         :dependencies [[pjstadig/humane-test-output "0.6.0"]
                        [org.clojure/tools.namespace "0.2.9"]]
         :injections [(require 'pjstadig.humane-test-output)
                      (pjstadig.humane-test-output/activate!)]}})
