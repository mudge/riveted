(ns riveted.core-test
  (:use midje.sweet riveted.core)
  (:import com.ximpleware.VTDNav))

;;; Test data.

(def xml "<root><!--Hello--><basic-title>Foo</basic-title><complex-title id=\"42\" empty=\"\"><i>Foo</i> woo <b>moo</b></complex-title><i>Bar</i><foo/></root>")
(def ns-xml "<root xmlns:dc=\"http://purl.org/dc/elements/1.1/\"><dc:name>Bob</dc:name></root>")

(def nav (navigator xml false))
(def ns-nav (navigator ns-xml true))

;;; Custom checkers to simplify testing.

(defn nav? [actual] (instance? riveted.core.Navigator actual))
(defn tag? [tag-name] (fn [actual] (= tag-name (tag actual))))
(defn tags? [& tag-names] (fn [actual] (= tag-names (map tag actual))))
(def root? (tag? "root"))

(fact "navigator returns a VTD navigator for a byte array."
  (navigator (.getBytes "<a></a>" "UTF-8")) => nav?
  (navigator (.getBytes "<a></a>" "ISO-8859-1")) => nav?)

(fact "navigator returns a VTD navigator for a UTF-8 string."
  (navigator "<a></a>") => nav?)

(fact "navigator raises an IllegalArgumentException nil if given nil."
  (navigator nil) => (throws IllegalArgumentException))

(fact "search returns a sequence of matching navigators for a given XPath."
  (search nav "/root/basic-title") => (one-of nav?)
  (search nav "//i") => (two-of nav?)
  (search nav "/missing") => empty?
  (search ns-nav "/root/foo:name" "foo" "http://purl.org/dc/elements/1.1/") => (one-of nav?)
  (search nil "/foo") => empty?)

(fact "at returns the first matching navigator for a given XPath."
  (at nav "/root/basic-title") => nav?
  (at nav "/missing") => nil?
  (at ns-nav "/root/foo:name" "foo" "http://purl.org/dc/elements/1.1/") => nav?
  (at nil "/foo") => nil?)

(fact "select returns navigators for all matching elements."
  (select nav "*") => (tags? "root" "basic-title" "complex-title" "i" "b" "i" "foo")
  (select nav "i") => (two-of (tag? "i"))
  (select nav :i) => (two-of (tag? "i"))
  (select (at nav "/root/complex-title") "*") => (tags? "complex-title" "i" "b")
  (select nav "missing") => empty?
  (select nil "foo") => empty?)

(fact "text returns the text nodes descending from a navigator."
  (text (at nav "/root/basic-title")) => "Foo"
  (text (at nav "/root/complex-title")) => "Foo woo moo"
  (text (at nav "/root/foo")) => nil?
  (text nil) => nil?)

(fact "fragment returns the content of the given navigator as an XML fragment."
  (fragment (at nav "/root/complex-title")) => "<i>Foo</i> woo <b>moo</b>"
  (fragment (at nav "/root/basic-title")) => "Foo"
  (fragment (at nav "/root/foo")) => ""
  (fragment nil) => nil?)

(fact "attr returns the value of the given attribute"
  (attr (at nav "/root/complex-title") :id) => "42"
  (attr (at nav "/root/complex-title") "id") => "42"
  (attr (at nav "/root/complex-title") :missing) => nil?
  (attr nil :foo) => nil?)

(fact "tag returns the current element name of the given navigator."
  (tag (root nav)) => "root"
  (tag (at nav "/root/complex-title")) => "complex-title"
  (tag nil) => nil?)

(fact "document? returns true if the navigator is set to the document."
  (parent (root nav)) => document?
  (root nav) =not=> document?
  (document? nil) => false)

(fact "element? returns true if the navigator is set to an element."
  (root nav) => element?
  (parent (root nav)) =not=> element?
  (element? nil) => false)

(fact "parent returns a navigator for the parent of the current navigator."
  (parent (at nav "/root/basic-title")) => nav?
  (parent nil) => nil?)

(fact "parent returns the document as the parent of the root."
  (parent (root nav)) => document?)

(fact "parent returns nil for the parent of the document."
  (parent (parent (root nav))) => nil?)

(fact "root returns a navigator for the root element."
  (root (at nav "/root/complex-title/i")) => nav?
  (root (at nav "/root/complex-title/i")) => root?)

(fact "first-child returns a navigator for the first child element."
  (first-child (at nav "/root/complex-title")) => nav?
  (first-child (at nav "/root/complex-title")) => (tag? "i")
  (first-child (at nav "/root/foo")) => nil?)

(fact "first-child takes an optional element name."
  (first-child (root nav) :complex-title) => (tag? "complex-title")
  (first-child (root nav) "complex-title") => (tag? "complex-title")
  (first-child (root nav) :missing) => nil?)

(fact "last-child returns a navigator for the last child element."
  (last-child (at nav "/root/complex-title")) => (tag? "b")
  (last-child (at nav "/root/foo")) => nil?)

(fact "last-child takes an optional element name."
  (last-child (root nav) :complex-title) => (tag? "complex-title")
  (last-child (root nav) "complex-title") => (tag? "complex-title")
  (last-child (root nav) :missing) => nil?)

(fact "next-sibling returns a navigator for the next sibling element."
  (next-sibling (at nav "/root/basic-title")) => (tag? "complex-title")
  (next-sibling (at nav "/root/complex-title")) => (tag? "i")
  (next-sibling (at nav "/root/foo")) => nil?)

(fact "next-sibling takes an optional element name."
  (next-sibling (at nav "/root/basic-title") :i) => (tag? "i")
  (next-sibling (at nav "/root/basic-title") "i") => (tag? "i")
  (next-sibling (at nav "/root/basic-title") :missing) => nil?)

(fact "previous-sibling returns a navigator for the previous sibling element."
  (previous-sibling (at nav "/root/complex-title")) => (tag? "basic-title")
  (previous-sibling (at nav "/root/foo")) => (tag? "i")
  (previous-sibling (at nav "/root/basic-title")) => nil?)

(fact "previous-sibling takes an optional element name."
  (previous-sibling (at nav "/root/i") :basic-title) => (tag? "basic-title")
  (previous-sibling (at nav "/root/i") "basic-title") => (tag? "basic-title")
  (previous-sibling (at nav "/root/i") :missing) => nil?)

(fact "next-siblings returns navigators for all next sibling elements."
  (next-siblings (at nav "/root/basic-title")) => (tags? "complex-title" "i"
                                                         "foo")
  (next-siblings (at nav "/root/foo")) => empty?)

(fact "next-siblings takes an optional element name."
  (next-siblings (at nav "/root/basic-title") :i) => (tags? "i")
  (next-siblings (at nav "/root/basic-title") "i") => (tags? "i")
  (next-siblings (at nav "/root/basic-title") :missing) => empty?)

(fact "previous-siblings returns navigators for all previous sibling elements."
  (previous-siblings (at nav "/root/foo")) => (tags? "i" "complex-title"
                                                     "basic-title")
  (previous-siblings (at nav "/root/basic-title")) => empty?)

(fact "previous-siblings takes an optional element name."
  (previous-siblings (at nav "/root/foo") :i) => (tags? "i")
  (previous-siblings (at nav "/root/foo") "i") => (tags? "i")
  (previous-siblings (at nav "/root/foo") :missing) => empty?)

(fact "siblings returns navigators for all sibling elements."
  (siblings (at nav "/root/basic-title")) => (tags? "complex-title" "i" "foo")
  (siblings (at nav "/root/complex-title")) => (tags? "basic-title" "i" "foo")
  (siblings (at nav "/root/i")) => (tags? "basic-title" "complex-title" "foo")
  (siblings (root nav)) => empty?)

(fact "siblings takes an optional element name."
  (siblings (at nav "/root/basic-title")
            :complex-title) => (tags? "complex-title")
  (siblings (at nav "/root/basic-title")
            "complex-title") => (tags? "complex-title")
  (siblings (at nav "/root/basic-title") :missing) => empty?)

(fact "children returns navigators for all children elements."
  (children (root nav)) => (tags? "basic-title" "complex-title" "i" "foo")
  (children (at nav "/root/foo")) => empty?)

(fact "children takes an optional element name."
  (children (root nav) :complex-title) => (tags? "complex-title")
  (children (root nav) "complex-title") => (tags? "complex-title")
  (children (root nav) :missing) => empty?)

(fact "attr? returns whether or not a given attribute exists."
  (attr? (at nav "/root/complex-title") :id) => true
  (attr? (at nav "/root/complex-title") "id") => true
  (attr? (at nav "/root/complex-title") :missing) => false)

;;; Mutable tests. Note that the order of these facts is critical.

(def nav! (navigator "<root><child><name>Foo</name><age>42</age></child><bro>Bar</bro></root>" false))

(fact "root! moves the given navigator to the root."
  (root! nav!) => (tag? "root"))

(fact "first-child! moves the given navigator to the first child."
  (first-child! nav!) => (tag? "child"))

(fact "first-child! takes an optional element name."
  (first-child! nav! :name) => (tag? "name")
  (first-child! nav! :missing) => nil?)

(fact "parent! moves the given navigator to the parent element."
  (parent! nav!) => (tag? "child"))

(fact "next-sibling! moves to the given navigator to the next sibling element."
  (next-sibling! nav!) => (tag? "bro")
  (next-sibling! nav!) => nil?)

(fact "next-sibling! takes an optional element name."
  (next-sibling! (previous-sibling! nav!) :bro) => (tag? "bro")
  (next-sibling! (previous-sibling! nav!) "bro") => (tag? "bro")
  (next-sibling! nav! :missing) => nil?)

(fact "previous-sibling! moves the given navigator to the previous sibling."
  (previous-sibling! nav!) => (tag? "child")
  (previous-sibling! nav!) => nil?)

(fact "previous-sibling! takes an optional element name."
  (previous-sibling! (next-sibling! nav!) :child) => (tag? "child")
  (previous-sibling! (next-sibling! nav!) "child") => (tag? "child")
  (previous-sibling! nav! :missing) => nil?)

(fact "last-child! moves the given navigator to the last child."
  (last-child! nav!) => (tag? "age"))

(fact "navigators are sequential."
  nav => sequential?)

(fact "navigators are counted."
  nav => counted?
  (count nav) => 18)

(fact "navigators expose all internal tokens as a seq."
  (first nav)  => {:type :start-tag, :value "root"}
  (second nav) => {:type :comment, :value "Hello"}
  (nth nav 2)  => {:type :start-tag, :value "basic-title"}
  (nth nav 3)  => {:type :character-data, :value "Foo"}
  (nth nav 5)  => {:type :attribute-name, :value "id"}
  (nth nav 6)  => {:type :attribute-value, :value "42"})

(fact "navigators not at the root, seq the remaining nodes."
  (first (first-child nav :complex-title)) => {:type :start-tag,
                                               :value "complex-title"}
  (last (first-child nav :complex-title)) => {:type :start-tag,
                                              :value "foo"})

(fact "navigators can safely be threaded even with nils."
  (-> nav (first-child :missing) (last-child :missing) text) => nil?)

(fact "attribute? returns true if the navigator is set to an attribute."
  (at nav "/root/complex-title/@id") => attribute?
  (at nav "/root/complex-title") =not=> attribute?
  (root nav) =not=> attribute?)

(fact "attribute values can be retrieved with text."
  (text (at nav "/root/complex-title/@id")) => "42"
  (text (at nav "/root/complex-title/@empty")) => "")

(fact "attribute names can be retrieved with tag."
  (tag (at nav "/root/complex-title/@id")) => "id"
  (tag (at nav "/root/complex-title/@empty")) => "empty")

