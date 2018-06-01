# embedded-kafka

[![Circle CI](https://circleci.com/gh/Mayvenn/embedded-kafka.svg)](https://circleci.com/gh/Mayvenn/embedded-kafka)

A library to run an in-process Kafka broker (and the necessary Zookeeper).

At Mayvenn we use this library alongside tests that integrate with Kafka without needing the Kafka/Zookeeper to be running separately. The test broker is created in a temp directory that is deleted each time the broker is recreated, making sure to avoid test pollution. Embedding these dependencies also allows these tests to work on CI as well without requiring any extra setup.

This library is a slightly simplified/extracted version of what is used in [clj-kafka](https://github.com/pingles/clj-kafka/blob/0.3.2/test/clj_kafka/test/utils.clj) for it to test itself. In the spirit of more end-to-end tests, we've found it quite useful to add just that part as a development dependency to most of our projects.

## Usage

The main functions simply create an in-process broker or zookeeper. Most of the time, the convenience macro `with-test-broker` is all that is used. This macro provides a producer and consumer that have the appropriate configuration for the in-process broker. If instantiating your own consumer/producers, the config is available as `kafka-config` in order to communicate with the appropriate broker. This config is declared `^:dynamic` in case there's a need to customize.

```clj
(ns ...
  (:require [embedded-kafka.core :refer [with-test-broker]]
            [gregor.core :as gregor]))

(with-test-broker producer consumer
  (gregor/subscribe consumer ["test-topic"])
  (gregor/create-topic {:connection-string "127.0.0.1:2182"} "test-topic" {})
  (.get (gregor/send producer "test-topic" "message-content"))
  (->> (gregor/poll consumer 15000)
       (take 1)
       first
       :value
       String.)) ;; "message-content"
```

## License
This project is released under EPL, which is viewable [HERE.](LICENSE)
