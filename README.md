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
[riveted "0.0.2"]
```

## Usage

```clojure
(ns foo
  (:require [riveted.core :as vtd]))

;; Create an initial navigator for the XML document in foo.xml (the second
;; argument toggles namespace awareness).
(def nav (vtd/navigator (slurp "foo.xml") false))

(def bold-words
  (vtd/search nav "//b"))
;=> returns a lazy sequence of navigators for each matching element

(def title
  (vtd/at nav "/article/front/article-meta/title-group/article-title"))
;=> returns another navigator that can be interrogated...

(vtd/tag title)
;=> "article-title"

(vtd/text title)
;=> "Some title"

(vtd/attr title :id)
;=> "123"

(vtd/fragment title)
;=> "<b>Some</b> title"

(vtd/parent title)
;=> return a navigator for the parent element

(vtd/next-sibling title)
;=> return a navigator for the next sibling element

(vtd/first-child title)
;=> return a navigator for the first child element

(vtd/last-child title)
;=> return a navigator for the last child element

(vtd/siblings title)
;=> return a lazy sequence of all sibling elements

(vtd/children title)
;=> return a lazy sequence of all children elements (note this does not
;   include text nodes)
```

## To Do

* Namespace-awareness;
* Zipper helper.

## Acknowledgements

[Andrew Diamond's `clj-vtd-xml`](https://github.com/diamondap/clj-vtd-xml) and
[Tim Williams' gist](https://gist.github.com/willtim/822769) are existing
interfaces to VTD-XML from Clojure that were great sources of inspiration.

## License

Copyright © 2013 Paul Mucur.

Distributed under the Eclipse Public License, the same as Clojure.
