(ns embedded-kafka.core-test
  (:require [clojure.test :refer :all]
            [embedded-kafka.core :refer [with-test-broker]]
            [clj-kafka.producer :refer [send-message message]]
            [clj-kafka.consumer.zk :refer [messages]]))

(deftest using-test-broker
  (with-test-broker producer consumer
    (send-message producer (message "test-topic" "message-content"))
    (is (= "message-content" (-> (messages consumer "test-topic")
                                 (first)
                                 (:value)
                                 (String.))))))
