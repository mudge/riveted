(ns riveted.core-test
  (:use clojure.test
        riveted.core))

(def xml "<root><basic-title>Foo</basic-title><complex-title id=\"42\"><i>Foo</i> woo <b>moo</b></complex-title><i>Bar</i><foo/></root>")
(def ns-xml "<root xmlns:dc=\"http://purl.org/dc/elements/1.1/\"><dc:name>Bob</dc:name></root>")

(def nav (navigator xml false))
(def ns-nav (navigator ns-xml true))

(deftest test-search
  (testing "Searches by XPath"
    (is (= 1 (count (search nav "/root/basic-title"))))
    (is (= 2 (count (search nav "//i"))))
    (is (empty? (search nav "/missing"))))
  (testing "Allows searching by XPath with namespaces"
    (is (= 1 (count (search ns-nav "/root/foo:name" "foo" "http://purl.org/dc/elements/1.1/"))))))

(deftest test-at
  (testing "Searches by XPath, returning the first match"
    (is (= "basic-title" (tag (at nav "/root/basic-title"))))
    (is (nil? (at nav "/missing"))))
  (testing "Allows searching by XPath with namespaces"
    (is (= "Bob" (text (at ns-nav "/root/foo:name" "foo" "http://purl.org/dc/elements/1.1/"))))))

(deftest test-navigate
  (testing "Only supports valid directions"
    (is (thrown? AssertionError (navigate nav -2)))
    (is (thrown? AssertionError (navigate nav 7)))))

(deftest test-text
  (testing "Returns text from simple nodes"
    (is (= "Foo" (text (at nav "/root/basic-title")))))
  (testing "Returns all text from mixed content nodes"
    (is (= "Foo woo moo" (text (at nav "/root/complex-title")))))
  (testing "Returns nil for empty nodes"
    (is (nil? (text (at nav "/root/foo"))))))

(deftest test-fragment
  (testing "Returns a content fragment for the contents of the given node"
    (is (= "<i>Foo</i> woo <b>moo</b>" (fragment (at nav "/root/complex-title"))))
    (is (= "Foo" (fragment (at nav "/root/basic-title"))))))

(deftest test-attr
  (testing "Returns the value of the given attribute"
    (is (= "42" (attr (at nav "/root/complex-title") :id)))
    (is (= "42" (attr (at nav "/root/complex-title") "id")))
    (is (nil? (attr (at nav "/root/complex-title") :missing)))))

(deftest test-document?
  (testing "Returns true if the navigator is set to the document"
    (is (document? (parent (root nav))))
    (is (not (document? (root nav))))))

(deftest test-element?
  (testing "Returns true if the navigator is set to an element"
    (is (element? (root nav)))
    (is (not (element? (parent (root nav)))))))

(deftest test-parent
  (testing "Returns a navigator for the parent element"
    (is (= "root" (tag (parent (at nav "/root/basic-title"))))))
  (testing "Returns the document when asking the parent of the root"
    (is (document? (parent (root nav)))))
  (testing "Returns nil when asking for the parent of the document"
    (is (nil? (parent (parent (root nav)))))))

(deftest test-root
  (testing "Returns a navigator for the root element"
    (is (= "root" (tag (root (at nav "/root/complex-title/i")))))))

(deftest test-first-child
  (testing "Returns a navigator for the first child element"
    (is (= "i" (tag (first-child (at nav "/root/complex-title")))))
    (is (nil? (first-child (at nav "/root/foo")))))
  (testing "Takes an optional element name"
    (is (= "complex-title" (tag (first-child (root nav) :complex-title))))
    (is (= "complex-title" (tag (first-child (root nav) "complex-title"))))
    (is (nil? (first-child (root nav) :missing)))))

(deftest test-last-child
  (testing "Returns a navigator for the last child element"
    (is (= "b" (tag (last-child (at nav "/root/complex-title")))))
    (is (nil? (last-child (at nav "/root/foo")))))
  (testing "Takes an optional element name"
    (is (= "complex-title" (tag (last-child (root nav) :complex-title))))
    (is (= "complex-title" (tag (last-child (root nav) "complex-title"))))
    (is (nil? (last-child (root nav) :missing)))))

(deftest test-next-sibling
  (testing "Returns the next sibling element"
    (is (= "complex-title" (tag (next-sibling (at nav "/root/basic-title")))))
    (is (= "i" (tag (next-sibling (at nav "/root/complex-title")))))
    (is (nil? (next-sibling (at nav "/root/foo")))))
  (testing "Takes an optional element name"
    (is (= "i" (tag (next-sibling (at nav "/root/basic-title") :i))))
    (is (= "i" (tag (next-sibling (at nav "/root/basic-title") "i"))))
    (is (nil? (next-sibling (at nav "/root/basic-title") :missing)))))

(deftest test-previous-sibling
  (testing "Returns the previous sibling element"
    (is (= "basic-title" (tag (previous-sibling (at nav "/root/complex-title")))))
    (is (= "i" (tag (previous-sibling (at nav "/root/foo")))))
    (is (nil? (previous-sibling (at nav "/root/basic-title")))))
  (testing "Takes an optional element name"
    (is (= "basic-title" (tag (previous-sibling (at nav "/root/i") :basic-title))))
    (is (= "basic-title" (tag (previous-sibling (at nav "/root/i") "basic-title"))))
    (is (nil? (previous-sibling (at nav "/root/i") :missing)))))

(deftest test-siblings
  (testing "Returns all sibling elements"
    (is (= ["complex-title" "i" "foo"] (map tag (siblings (at nav "/root/basic-title")))))
    (is (= ["basic-title" "i" "foo"] (map tag (siblings (at nav "/root/complex-title")))))
    (is (= ["basic-title" "complex-title" "foo"] (map tag (siblings (at nav "/root/i")))))
    (is (nil? (siblings (root nav)))))
  (testing "Takes an optional element name"
    (is (= ["complex-title"] (map tag (siblings (at nav "/root/basic-title") :complex-title))))
    (is (= ["complex-title"]
           (map tag (siblings (at nav "/root/basic-title") "complex-title"))))
    (is (nil? (siblings (at nav "/root/basic-title") :missing)))))

(deftest test-children
  (testing "Returns all child elements"
    (is (= ["basic-title" "complex-title" "i" "foo"]
           (map tag (children (root nav)))))
    (is (nil? (children (at nav "/root/foo")))))
  (testing "Takes an optional element name"
    (is (= ["complex-title"] (map tag (children (root nav) :complex-title)))))
    (is (= ["complex-title"] (map tag (children (root nav) "complex-title"))))
    (is (nil? (children (root nav) :missing))))

(deftest test-attr?
  (testing "Checks whether a given attribute exists on an element"
    (is (attr? (at nav "/root/complex-title") :id))
    (is (attr? (at nav "/root/complex-title") "id"))
    (is (not (attr? (at nav "/root/complex-title") :missing)))))

