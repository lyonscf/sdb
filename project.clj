(defproject org.clojars.erikcw/sdb "1.0.0-SNAPSHOT"
  :description "A Clojure library for working with Amazon SimpleDB"
  :dependencies [[log4j/log4j "1.2.15" :exclusions [javax.mail/mail
                                                   javax.jms/jms
                                                   com.sun.jdmk/jmxtools
                                                   com.sun.jmx/jmxri]]
                 [com.amazonaws/aws-java-sdk "1.0.002"]
                 [org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]]
  :dev-dependencies [[swank-clojure "1.2.1"]
                     [lein-clojars "0.5.0-SNAPSHOT"]]
  :source-path "src")
