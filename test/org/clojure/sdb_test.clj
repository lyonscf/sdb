(ns org.clojure.sdb-test
  "This test suite will run some basic tests against a live Amazon SimpleDB account.  You may want to use a Sandbox/Mock DB service for testing.  The test suite will use a random domain name ('sdb-test-UUID') to prevent conflicts with your existing data."
  (:use [org.clojure.sdb] :reload-all)
  (:use [clojure.test]))


; Be careful not to save your credentials into a public repo.
(def *AWS-ACCESS-KEY-ID* "your-aws-access-key")
(def *AWS-SECRET-ACCESS-KEY* "your-aws-secret-key")

;get logging to calm down, else noisy at REPL 
(org.apache.log4j.PropertyConfigurator/configure 
 (doto (java.util.Properties.) 
   (.setProperty "log4j.rootLogger" "WARN")
   (.setProperty "log4j.logger.com.amazonaws.sdb" "WARN") 
   (.setProperty "log4j.logger.httpclient" "WARN")))

(defn sleep
  "SimpleDB is 'eventually consistent', which means it can take a few seconds for data modifications to be reflected in data reads.  For testing purposes, it can be useful to pause execution for a moment to prevent tests from failing."
  ([] (sleep 1000))
  ([ms] (Thread/sleep ms)))

(defn setup-fixture []
  (def client (create-client *AWS-ACCESS-KEY-ID* *AWS-SECRET-ACCESS-KEY*))
  (def *test-domain-name* (str "sdb-test-"
			       (.toString (java.util.UUID/randomUUID))))
  (def *test-domain-created* (ref false))

  (if (not-any? #{*test-domain-name*} (domains client))
    (do
      (create-domain client *test-domain-name*)
      (dosync (ref-set *test-domain-created* true)))
    (dosync (ref-set *test-domain-created* false)))

;the 'spreadsheet' sample data in the canonic item-map representation
;an item-map must have an :sdb/id key. Multi-value attrs are represented as sets. 
;Note that attrs with single values always come back as non-sets!
  (def db-data
     #{{:sdb/id (uuid "773fb848-70a2-4586-b6b3-4aaa2dd55e00"),
        :category #{"Clothing" "Motorcycle Parts"},
        :subcat "Clothing",
        :Name "Leather Pants",
        :color "Black",
        :size #{"Small" "Medium" "Large"}}
       {:sdb/id (uuid "e76589f6-34e5-4a14-8af6-b70bf0242d7d"),
        :category "Clothes",
        :subcat "Pants",
        :Name "Sweatpants",
        :color #{"Yellow" "Pink" "Blue"},
        :size "Large"}
       {:sdb/id (uuid "7658a719-837a-4f55-828b-2472cc823bb1"),
        :category "Clothes",
        :subcat "Sweater",
        :Name "Cathair Sweater",
        :color "Siamese",
        :size #{"Small" "Medium" "Large"}}
       {:sdb/id (uuid "fa18dc85-63d3-47b0-865b-7946057d7f42"),
        :category "Car Parts",
        :subcat "Emissions",
        :Name "02 Sensor",
        :make "Audi",
        :model "S4"}
       {:sdb/id (uuid "6bca3ac8-fc8d-44d2-b7c3-da959ad7dfc4"),
        :category "Clothes",
        :subcat "Pants",
        :Name "Designer Jeans",
        :color #{"Paisley Acid Wash"},
        :size #{"32x32" "30x32" "32x34"}}
       {:sdb/id (uuid "ebd3cb4a-b94d-4150-baaa-0821beaa776c"),
        :category "Car Parts",
        :subcat "Engine",
        :Name "Turbos",
        :make "Audi",
        :model "S4"}
       {:sdb/id (uuid "598d2273-ae72-4946-9fa4-1fe4a2874be7"),
        :category "Motorcycle Parts",
        :subcat "Bodywork",
        :Name "Fender Eliminator",
        :color "Blue",
        :make "Yamaha",
        :model "R1"}})

  ;put the data into SDB
  (batch-put-attrs client *test-domain-name* db-data)
  (sleep 1000))

(defn tear-down-fixture []
  ; Tear-down
  (if @*test-domain-created*
    (delete-domain client *test-domain-name*)
    (prn "The test database wasn't setup by this test run.  Skip delete for safety.")))
  

(deftest test-domains
  (is (= true (if (some #{*test-domain-name*} (domains client)) true false))))


(deftest test-domain-metadata
  (is (= true (map? (domain-metadata client *test-domain-name*)))))


(deftest test-get-one
  (is (= (:category (get-attrs client *test-domain-name* (uuid "e76589f6-34e5-4a14-8af6-b70bf0242d7d"))) "Clothes")))
        ;{:category "Clothes", :color #{"Yellow" "Pink" "Blue"}, :Name "Sweatpants", :size "Large", :subcat "Pants", :sdb/id #<UUID e76589f6-34e5-4a14-8af6-b70bf0242d7d>})))

(deftest test-get-part-of-one
  (let [item  (get-attrs client *test-domain-name* (uuid "e76589f6-34e5-4a14-8af6-b70bf0242d7d") :color :Name)]
    (is (= (:Name item) "Sweatpants"))
    (is (= (:color item) #{"Yellow" "Pink" "Blue"} ))))

(deftest test-delete-attrs
  (is (= (:size
	  (get-attrs client *test-domain-name* (uuid "6bca3ac8-fc8d-44d2-b7c3-da959ad7dfc4")))
	 #{"32x32" "30x32" "32x34"}))
  (delete-attrs client *test-domain-name* (uuid "6bca3ac8-fc8d-44d2-b7c3-da959ad7dfc4") {:size "32x32"})
  (sleep)
  (is (= (:size
	  (get-attrs client *test-domain-name* (uuid "6bca3ac8-fc8d-44d2-b7c3-da959ad7dfc4")))
	#{"30x32" "32x34"})))

(deftest test-delete-record
  (is (= (:Name
	  (get-attrs client *test-domain-name* (uuid "e76589f6-34e5-4a14-8af6-b70bf0242d7d")))
	 "Sweatpants"))
  ;wipe it out
  (delete-attrs client *test-domain-name* (uuid "e76589f6-34e5-4a14-8af6-b70bf0242d7d"))
  (sleep 1000)
  (let [item (get-attrs client *test-domain-name* (uuid "e76589f6-34e5-4a14-8af6-b70bf0242d7d"))]
    (is (nil? (:Name item)))
    (is (nil? (:color item)))
    (is (nil? (:category item))))
	  
  ;restore it
  (put-attrs client *test-domain-name*
	     {:sdb/id (uuid "e76589f6-34e5-4a14-8af6-b70bf0242d7d"),
	      :category "Clothes",
	      :subcat "Pants",
	      :Name "Sweatpants",
	      :color #{"Yellow" "Pink" "Blue"},
	      :size "Large"})
  (sleep)
  (is (= (:Name
	  (get-attrs client *test-domain-name* (uuid "e76589f6-34e5-4a14-8af6-b70bf0242d7d")))
	 "Sweatpants")))

(deftest test-remove-bits
  (let [item (get-attrs client *test-domain-name* (uuid "e76589f6-34e5-4a14-8af6-b70bf0242d7d"))]
    (is (= (:Name item) "Sweatpants"))
    (is (= (:size item) "Large")))
  ; Delete the Name and size attributes
  (delete-attrs client *test-domain-name* (uuid "e76589f6-34e5-4a14-8af6-b70bf0242d7d") #{:Name :size})
   (let [item (get-attrs client *test-domain-name* (uuid "e76589f6-34e5-4a14-8af6-b70bf0242d7d"))]
    (is (nil? (:Name item)))
    (is (nil? (:size item))))
   (is (= (:color
	   (get-attrs client *test-domain-name* (uuid "e76589f6-34e5-4a14-8af6-b70bf0242d7d")))
	  #{"Yellow" "Pink" "Blue"}))
   ; Delete the color Yellow
   (delete-attrs client *test-domain-name* (uuid "e76589f6-34e5-4a14-8af6-b70bf0242d7d") {:color "Yellow"})
   (sleep)
   (is (= (:color
	   (get-attrs client *test-domain-name* (uuid "e76589f6-34e5-4a14-8af6-b70bf0242d7d")))
	  #{"Pink" "Blue"})))

(deftest test-query
  (let [result (query client
		      '{;:select ids
			;:select count
			:select *
			;:select [:Name, :color]
			:from "test"
			:where (or
				(and (= :category "Clothes") (= :subcat "Pants"))
				(= :category "Car Parts"))})]
    (is (= (count result) 4) "Confirm that 4 records were returned")
    (is (= (:model (first result) "S4"))))

  ;parameterized query is just syntax-quote!
  (let [cat "Clothes"
	result (query client `{:select * :from "test" :where (or (= :category ~cat)
							  (= :category "Car Parts"))})]
    (is (= (count result) 5) "Confirm that 5 records were returned")
    (is (= (:model (first result) "S4")))))


(defn test-ns-hook
  "Use this hook instead of use-fixtures so that we can control setup and execution order."
  []
  (setup-fixture)
  (test-domains)
  (test-domain-metadata)
  (test-get-one)
  (test-get-part-of-one)
  (test-delete-attrs)
  (test-delete-record)
  (test-remove-bits)
  (test-query)
  (tear-down-fixture))