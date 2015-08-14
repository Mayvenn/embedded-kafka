# embedded-kafka

[![Circle CI](https://circleci.com/gh/Mayvenn/embedded-kafka.png?circle-token=9c1aec1f3d8ff124bf8a729b17402ca63beede0c)](https://circleci.com/gh/Mayvenn/embedded-kafka)

A library to run an in process Kafka broker (and the necessary Zookeeper).

At Mayvenn we use this library alongside tests that integrate with Kafka without needing the Kafka/Zookeeper to be running separately. The test broker is created in a temp directory that is deleted each time the broker is recreated, making sure to avoid test pollution. Embedding these dependencies also allows these tests to work on CI as well without requiring any extra setup.

This library is a slightly simplified/extracted version of what is used in [clj-kafka](https://github.com/pingles/clj-kafka/blob/0.3.2/test/clj_kafka/test/utils.clj) for it to test itself. In the spirit of more end to end tests, we've found it quite useful to add just that part as a development dependency to most of our projects.

## Usage

The main functions simply create an in process broker or zookeeper. Most of the time, the convenience macro `with-test-broker` is all that is used. This macro provides a producer and consumer that have the appropriate configuration for the in process broker. If instantiating your own consumer/producers, the config is available as `kafka-config` in order to communicate with the appropriate broker. This config is declared as dynamic as well in case there's a need to customize.

```clj
(ns ...
    (:require [embedded-kafka :refer [with-test-broker]]
              [clj-kafka.producer :refer [send-message message]]
              [clj-kafka.consumer.zk :refer [messages]]))

(with-test-broker producer consumer
  (send-message producer (message "test-topic" "message-content"))
  (-> (messages consumer "test-topic")
      first
      :value
      String.)) ;; "message-content"
```
