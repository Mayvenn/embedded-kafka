(defproject embedded-kafka "0.5.0"
  :description "Helpers to run an embedded Kafka Broker"
  :url "https://github.com/Mayvenn/embedded-kafka"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [commons-io/commons-io "2.4"]
                 [io.weft/gregor "0.6.0"]
                 [org.apache.kafka/kafka_2.11 "0.11.0.1"]]
  :deploy-repositories [["releases" :clojars]]
  :profiles
  {:dev {:source-paths ["dev"]
         :dependencies [[diff-eq "0.2.5"]
                        [org.clojure/tools.namespace "0.2.9"]]
         :plugins [[lein-cljfmt "0.3.0"]]
         :injections [(require 'diff-eq.core)
                      (diff-eq.core/diff!)]}})
