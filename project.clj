(defproject org.clojars.erikcw/sdb "1.0.0-SNAPSHOT"
  :description "A Clojure library for working with Amazon SimpleDB"
  :dependencies [[log4j/log4j "1.2.15"]
                 [com.amazonaws/aws-java-sdk "1.0.002"]
                 [org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]]
  :dev-dependencies [[leiningen/lein-swank "1.1.0"]
                     [lein-clojars "0.5.0-SNAPSHOT"]]
  :source-path "src")
