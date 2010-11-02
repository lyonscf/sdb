(defproject org.clojars.markgunnels/sdb "1.0.1-SNAPSHOT"
  :description "A Clojure library for working with Amazon SimpleDB"
  :dependencies [[log4j/log4j "1.2.15" :exclusions [javax.mail/mail
                                                   javax.jms/jms
                                                   com.sun.jdmk/jmxtools
                                                   com.sun.jmx/jmxri]]
                 [com.amazonaws/aws-java-sdk "1.0.007"]
                 [org.clojure/clojure "1.2.0-master-SNAPSHOT"]
                 [org.clojure/clojure-contrib "1.2.0-SNAPSHOT"]
                 [com.google.code.typica/typica "1.5.2a"]]
  :dev-dependencies [[swank-clojure "1.2.1"]
                     [lein-clojars "0.5.0-SNAPSHOT"]]
  :source-path "src")
