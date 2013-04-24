(ns riveted.core-test
  (:use clojure.test
        riveted.core))

(def xml "<root><basic-title>Foo</basic-title><complex-title><i>Foo</i> woo <b>moo</b></complex-title></root>")

(def nav (navigator xml false))

(deftest test-search
  (testing "Searches by XPath"
    (is (= 1 (count (search nav "/root/basic-title"))))))

(deftest test-text
  (testing "Returns text from simple nodes"
    (is (= "Foo" (text (at nav "/root/basic-title")))))
  (testing "Returns text from complex nodes"
    (is (= "Foo woo moo" (text (at nav "/root/complex-title"))))))

