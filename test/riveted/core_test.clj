(ns riveted.core-test
  (:use clojure.test
        riveted.core))

(def xml "<root><basic-title>Foo</basic-title><complex-title id=\"42\"><i>Foo</i> woo <b>moo</b></complex-title><i>Bar</i></root>")

(def nav (navigator xml false))

(deftest test-search
  (testing "Searches by XPath"
    (is (= 1 (count (search nav "/root/basic-title"))))
    (is (= 2 (count (search nav "//i"))))
    (is (empty? (search nav "/missing")))))

(deftest test-text
  (testing "Returns text from simple nodes"
    (is (= "Foo" (text (at nav "/root/basic-title")))))
  (testing "Returns all text from mixed content nodes"
    (is (= "Foo woo moo" (text (at nav "/root/complex-title"))))))

(deftest test-fragment
  (testing "Returns a content fragment for the contents of the given node"
    (is (= "<i>Foo</i> woo <b>moo</b>" (fragment (at nav "/root/complex-title"))))
    (is (= "Foo" (fragment (at nav "/root/basic-title"))))))

(deftest test-attr
  (testing "Returns the value of the given attribute"
    (is (= "42" (attr (at nav "/root/complex-title") :id)))
    (is (= "42" (attr (at nav "/root/complex-title") "id")))
    (is (nil? (attr (at nav "/root/complex-title") :missing)))))

(deftest test-parent
  (testing "Returns a navigator for the parent element"
    (is (= "root" (tag (parent (at nav "/root/basic-title")))))))

(deftest test-root
  (testing "Returns a navigator for the root element"
    (is (= "root" (tag (root (at nav "/root/complex-title/i")))))))

(deftest test-first-child
  (testing "Returns a navigator for the first child element"
    (is (= "i" (tag (first-child (at nav "/root/complex-title")))))))

(deftest test-last-child
  (testing "Returns a navigator for the last child element"
    (is (= "b" (tag (last-child (at nav "/root/complex-title")))))))
