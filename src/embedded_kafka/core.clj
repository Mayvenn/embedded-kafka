;; from https://github.com/pingles/clj-kafka/blob/0.2.8-0.8.1.1/test/clj_kafka/test/utils.cl

(ns embedded-kafka.core
  (:import
    [kafka.admin AdminUtils]
    [kafka.server KafkaConfig KafkaServer]
    [java.net InetSocketAddress]
    [org.apache.zookeeper.server ZooKeeperServer NIOServerCnxn$Factory]
    [org.apache.commons.io FileUtils]
    [org.I0Itec.zkclient ZkClient]
    [org.I0Itec.zkclient.serialize ZkSerializer]
    [kafka.utils Time]
    [java.util Properties])
  (:require [clojure.java.io :refer [file]]
            [clj-kafka.core :refer [as-properties]]
            [clj-kafka.producer :refer [producer]]
            [clj-kafka.consumer.zk :refer [consumer shutdown]]))

(defn tmp-dir
  [& parts]
  (.getPath (apply file (System/getProperty "java.io.tmpdir") "embedded-kafka" parts)))

(def system-time (proxy [Time] []
                   (milliseconds [] (System/currentTimeMillis))
                   (nanoseconds [] (System/nanoTime))
                   (sleep [ms] (Thread/sleep ms))))

(def ^:dynamic kafka-config
  {"broker.id"                   "0"
   "port"                        "9999"
   "host.name"                   "localhost"
   "metadata.broker.list"        "localhost:9999"
   "zookeeper.connect"           "127.0.0.1:2182"
   "zookeeper-port"              "2182"
   "log.flush.interval.messages" "1"
   "auto.create.topics.enable"   "true"
   "group.id"                    "consumer"
   "auto.offset.reset"           "smallest"
   "serializer.class"            "kafka.serializer.StringEncoder",
   "auto.commit.enable"          "false"
   "log.dir"                     (.getAbsolutePath (file (tmp-dir "kafka-log")))})

(defn create-broker
  []
  (KafkaServer. (KafkaConfig. (as-properties kafka-config))
                system-time))

(defn create-zookeeper
  []
  (let [tick-time 500
        zk (ZooKeeperServer. (file (tmp-dir "zookeeper-snapshot")) (file (tmp-dir "zookeeper-log")) tick-time)]
    (doto (NIOServerCnxn$Factory. (InetSocketAddress. "127.0.0.1" (read-string (kafka-config "zookeeper-port"))))
      (.startup zk))))

(def string-serializer (proxy [ZkSerializer] []
                         (serialize [data] (.getBytes data "UTF-8"))
                         (deserialize [bytes] (when bytes
                                                (String. bytes "UTF-8")))))

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
           (let [zk-client# (ZkClient. (kafka-config "zookeeper.connect") 500 500 string-serializer)]
             ~@body)
           (finally (do (shutdown ~consumer-name)
                        (.close ~producer-name)
                        (.shutdown kafka#)
                        (.awaitShutdown kafka#)
                        (.shutdown zk#)
                        (FileUtils/deleteDirectory (file (tmp-dir)))))))))
