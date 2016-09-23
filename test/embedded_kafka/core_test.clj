(ns embedded-kafka.core-test
  (:require [clojure.test :refer :all]
            [embedded-kafka.core :refer [with-test-broker] :as ek]
            [gregor.core :as gregor]))

(deftest using-test-broker
  (with-test-broker producer consumer
    (gregor/subscribe consumer ["test-topic"])
    (gregor/create-topic {:connection-string "127.0.0.1:2182"} "test-topic" {})
    (.get (gregor/send producer "test-topic" "message-content"))
    (is (= [{:topic "test-topic" :value "message-content"}]
           (->> (gregor/poll consumer 15000)
                (take 1)
                (map #(select-keys % [:topic :value])))))))

