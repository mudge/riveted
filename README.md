# riveted [![Build Status](https://travis-ci.org/mudge/riveted.png?branch=master)](https://travis-ci.org/mudge/riveted)

A Clojure interface for parsing XML with
[VTD-XML](http://vtd-xml.sourceforge.net).

It provides a more Clojure-like abstraction over VTD while still exposing the
ability to traverse a document's elements with `(first-child nav)`,
`(parent nav)`, `(next-sibling nav)`, etc.

Note that, unlike most XML libraries, you use selectors to return navigators
(viz. a cursor to a point within the document) that can then be interrogated
for their tag name, attributes and text values with other functions.

## Installation

As riveted is available on [Clojars](https://clojars.org/riveted), add the
following to your [Leiningen](https://github.com/technomancy/leiningen)
dependencies:

```clojure
[riveted "0.0.5"]
```

## Usage

```clojure
(ns foo
  (:require [riveted.core :as vtd]))

;; Create an initial navigator for the XML document in foo.xml
(def nav (vtd/navigator (slurp "foo.xml")))

(def bold-words
  (vtd/search nav "//b"))
;=> returns a lazy sequence of navigators for each matching element by XPath

(def title
  (vtd/at nav "/article/front/article-meta/title-group/article-title"))
;=> returns the first navigator that can be interrogated...

(def italic-words
  (vtd/select nav :i))
;=> returns a lazy sequence of navigators for each matching element by name

(def all-elements
  (vtd/select nav "*"))
;=> returns a lazy sequence of all elements

(vtd/tag title)
;=> "article-title"

(vtd/text title)
;=> "Some title"
;   (note that this will include *all* descendant text nodes
;   regardless of mixed content so title could have been
;   "<article-title><italic>Some</italic> title</article-title>")

(vtd/attr title :id)
;=> "123"

(vtd/attr? title :id)
;=> true when the element under the navigator has an attribute with the given
;   name

(vtd/fragment title)
;=> "<b>Some</b> title"

(vtd/parent title)
;=> return a navigator for the parent element

(vtd/root title)
;=> return a navigator for the root element

(vtd/next-sibling title)
;=> return a navigator for the next sibling element

(vtd/next-sibling title :author)
;=> return a navigator for the next sibling "author" element

(vtd/next-siblings title)
;=> return a lazy sequence of navigators for all next sibling elements

(vtd/next-siblings title :author)
;=> return a lazy sequence of navigators for all next sibling "author"
;   elements

(vtd/previous-sibling title)
;=> return a navigator for the previous sibling element

(vtd/previous-sibling title :author)
;=> return a navigator for the previous sibling "author" element

(vtd/previous-siblings title)
;=> return a lazy sequence of navigators for all previous sibling elements

(vtd/previous-siblings title :author)
;=> return a lazy sequence of navigators for all previous sibling "author"
;   elements

(vtd/first-child title)
;=> return a navigator for the first child element

(vtd/first-child title :b)
;=> return a navigator for the first child "b" element

(vtd/last-child title)
;=> return a navigator for the last child element

(vtd/last-child title :i)
;=> return a navigator for the last child "i" element

(vtd/siblings title)
;=> return a sequence of all sibling elements

(vtd/siblings title :span)
;=> return a sequence of all sibling "span" elements

(vtd/children title)
;=> return a lazy sequence of all children elements (note this does not
;   include text nodes)

(vtd/children title :p)
;=> return a lazy sequence of all children "p" elements

(vtd/document? title)
;=> true if the navigator is pointing to the whole document

(vtd/element? title)
;=> true if the navigator is pointing to an element

;; Create an initial navigator for the XML document in namespaced.xml with
;; namespace support enabled.
(def ns-nav (vtd/navigator (slurp "namespaced.xml") true))

(vtd/search ns-nav "//ns1:name" "ns1" "http://purl.org/dc/elements/1.1/")
;=> return a lazy sequence of navigators for matching elements with the given
;   namespace.
```

## Mutable Interface

riveted also provides a mutable interface to VTDNav (much like Clojure's
[transient](http://clojure.org/transients) data structures) for lower-memory
usage (at the cost of immutability):

```clojure
;; Create an initial navigator as per usual.
(def nav (navigator "<root><a>Foo</a><b>Bar</b></root>"))

;; Mutate nav to point to the a element.
(vtd/first-child! nav)

(vtd/text nav)
;=> "Foo"

;; Mutate nav to point to the b element.
(vtd/next-sibling! nav)

(vtd/text nav)
;=> "Bar"

;; Mutate nav to point to the a element again.
(vtd/previous-sibling! nav)

;; Mutate nav to point to the root element.
(vtd/parent! nav)

;; Mutate nav to point to the root of the document (regardless of location).
(vtd/root! nav)
```

## Acknowledgements

[Andrew Diamond's `clj-vtd-xml`](https://github.com/diamondap/clj-vtd-xml) and
[Tim Williams' gist](https://gist.github.com/willtim/822769) are existing
interfaces to VTD-XML from Clojure that were great sources of inspiration.

[Dave Ray's `seesaw`](https://github.com/daveray/seesaw) sets the standard for
helpful docstrings.

## License

Copyright © 2013 Paul Mucur.

Distributed under the Eclipse Public License, the same as Clojure.
