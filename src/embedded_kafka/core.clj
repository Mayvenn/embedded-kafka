;; from https://github.com/pingles/clj-kafka/blob/0.2.8-0.8.1.1/test/clj_kafka/test/utils.cl

(ns embedded-kafka.core
  (:import
    [kafka.server KafkaConfig KafkaServerStartable]
    [java.net InetSocketAddress]
    [org.apache.zookeeper.server ZooKeeperServer NIOServerCnxnFactory]
    [org.apache.commons.io FileUtils])
  (:require [clojure.java.io :refer [file]]
            [clj-kafka.core :refer [as-properties]]
            [clj-kafka.producer :refer [producer]]
            [clj-kafka.consumer.zk :refer [consumer shutdown]]))

(defn tmp-dir
  [& parts]
  (.getPath (apply file (System/getProperty "java.io.tmpdir") "embedded-kafka" parts)))

(def ^:dynamic kafka-config
  {"broker.id"                        "0"
   "port"                             "9999"
   "host.name"                        "localhost"
   "metadata.broker.list"             "localhost:9999"
   "zookeeper.connect"                "127.0.0.1:2182"
   "zookeeper-port"                   "2182"
   "offsets.topic.replication.factor" "1"
   "log.flush.interval.messages"      "1"
   "auto.create.topics.enable"        "true"
   "group.id"                         "consumer"
   "auto.offset.reset"                "smallest"
   "serializer.class"                 "kafka.serializer.StringEncoder",
   "retry.backoff.ms"                 "500"
   "message.send.max.retries"         "5"
   "auto.commit.enable"               "false"
   "log.dir"                          (.getAbsolutePath (file (tmp-dir "kafka-log")))})

(defn create-broker
  []
  (KafkaServerStartable. (KafkaConfig. (as-properties kafka-config))))

(defn create-zookeeper
  []
  (let [tick-time 500
        zk (ZooKeeperServer. (file (tmp-dir "zookeeper-snapshot")) (file (tmp-dir "zookeeper-log")) tick-time)]
    (doto (NIOServerCnxnFactory.)
      (.configure (InetSocketAddress. (read-string (kafka-config "zookeeper-port"))) 60)
      (.startup zk))))

(defmacro with-test-broker
  "Creates an in-process broker that can be used to test against"
  [producer-name consumer-name & body]
  `(do (FileUtils/deleteDirectory (file (tmp-dir)))
       (let [zk# (create-zookeeper)
             kafka# (create-broker)
             ~producer-name (producer kafka-config)
             ~consumer-name (consumer kafka-config)]
         (try
           (.startup kafka#)
           ~@body
         (finally (do (shutdown ~consumer-name)
                      (.close ~producer-name)
                      (.shutdown kafka#)
                      (.awaitShutdown kafka#)
                      (.shutdown zk#)
                      (FileUtils/deleteDirectory (file (tmp-dir)))))))))
