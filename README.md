# riveted [![Clojure CI](https://github.com/mudge/riveted/actions/workflows/clojure.yml/badge.svg)](https://github.com/mudge/riveted/actions/workflows/clojure.yml)

A Clojure library for the
[fast](http://vtd-xml.sourceforge.net/benchmark1.html) processing of XML with
[VTD-XML](http://vtd-xml.sourceforge.net), a [Virtual Token
Descriptor](http://vtd-xml.sf.net/VTD.html) XML parser.

It provides a more Clojure-like abstraction over VTD while still exposing the
power of its low-level interface.

## Installation

As riveted is available on [Clojars](https://clojars.org/riveted), add the
following to your [Leiningen](https://github.com/technomancy/leiningen)
dependencies:

```clojure
[riveted "0.2.0"]
```

## Compatibility

riveted is tested against Clojure 1.3, 1.4, 1.5.1, 1.6, 1.7, 1.8, 1.9, 1.10.0,
1.10.1, 1.10.2 and 1.10.3.

## API Documentation

The latest [riveted API documentation](http://mudge.name/riveted/) is
automatically generated with [Codox](https://github.com/weavejester/codox).

## Quick Start

For more details, see [Usage](#usage) below.

```clojure
(ns foo
  (:require [riveted.core :as vtd]))

(def nav (vtd/navigator (slurp "foo.xml")))

;; Navigating by direction and returning text content.
(-> nav vtd/first-child vtd/next-sibling vtd/text) ;=> "Foo"

;; Navigating by direction, restricted by element and returning attribute
;; value.
(-> nav (vtd/first-child :p) (attr :id)) ;=> "42"

;; Return the tag names of all children elements.
(->> nav vtd/children (map vtd/tag)) ;=> ("p" "a" "b")

;; Navigating by element name, regardless of location.
(-> nav (vtd/select :p) first vtd/text)

;; Navigating by XPath, returning all matches.
(map vtd/text (vtd/search nav "//author"))

;; Navigating by XPath, returning the first match.
(vtd/text (vtd/at nav "/article/title"))

;; Calling seq (or any function that uses seq such as first, second, nth,
;; last, etc.) on the navigator yields a sequence of all parsed tokens as
;; simple maps with a type and value entry.
(first nav) ;=> {:type :start-tag, :value "a"}
```

## Usage

Once installed, you can include riveted into your desired namespace by
requiring `riveted.core` like so:

```clojure
(ns foo
  (:require [riveted.core :as vtd]))
```

The core data structure in riveted is the navigator: this represents both your
XML document and your current location within it. It can be interrogated for
the tag name, attributes and text value of any given element and also provides
the ability to move around the document.

Let's say we have a file called `foo.xml` with the following content:

```xml
<article>
  <title>Foo bar</title>
  <author id="1">
    <name>Robert Paulson</name>
    <name>Joe Bloggs</name>
  </author>
  <abstract>
    A <i>great</i> article all about things.
  </abstract>
</article>
```

Let's load this into an initial navigator with the `navigator` function,
passing it a UTF-8 encoded string of XML and then storing the result in the
[var](http://clojure.org/vars) `nav`:

```clojure
(def nav (vtd/navigator (slurp "foo.xml")))
```

If you already have your XML in a byte array, you can pass this directly to `navigator` instead of a UTF-8 string:

```clojure
(def nav (vtd/navigator my-byte-array))
```

`navigator` also takes an optional second argument to enable XML namespace
support which is disabled by default. We'll look at this
[later](#namespace-support) but, for now, we can process this document without
using namespaces.

Now that we have a navigator, we can navigate the document in several ways
(c.f. [VTD-XML's explanation of its different
views](http://vtd-xml.sourceforge.net/userGuide/3.html)):

* As a [cursor-based hierarchical view](#traversing-by-direction);
* Using [element selectors](#traversing-by-element-name);
* Using [XPath](#traversing-by-xpath);
* As a [flat view of tokens](#flat-view-of-tokens).

There is also a [mutable interface](#mutable-interface) for more constrained
memory usage.

### Traversing by direction

After parsing a document, the navigator's cursor is always at the root element
of our XML: for `foo.xml`, this means the `article` element. If we want to
retrieve the `title` and we know it's the first child of the article we can
simply use riveted's `first-child` function:

```clojure
(vtd/first-child nav)
```

This returns a new navigator with its cursor set to the `title` element. We
can check this by using the `text` and `tag` functions to return the text
content and tag name of the current cursor respectively:

```clojure
(vtd/text (vtd/first-child nav)) ;=> "Foo bar"
(vtd/tag (vtd/first-child nav))  ;=> "title"
```

If we then want to move to the `author` element, we can use the `next-sibling`
function in a similar way:

```clojure
(vtd/next-sibling (vtd/first-child nav))
```

It may be more readable to use Clojure's [threading macro,
`->`](http://clojuredocs.org/clojure_core/clojure.core/-%3E) when traversing
in multiple directions:

```clojure
(-> nav vtd/first-child vtd/next-sibling)
```

If we want to test an element for its attributes, we can use `attr?` like so:

```clojure
(-> nav vtd/first-child vtd/next-sibling (vtd/attr? :id)) ;=> true
```

We can then fetch the value of the attribute with `attr`:

```clojure
(-> nav vtd/first-child vtd/next-sibling (vtd/attr :id)) ;=> "1"

;; equivalent to:
(vtd/attr (vtd/next-sibling (vtd/first-child nav)) :id)
```

As well as `first-child` and `next-sibling`, you can move in one direction
with the following functions:

```clojure
(vtd/previous-sibling nav) ;=> move to the previous sibling element
(vtd/last-child nav)       ;=> move to the last child element
(vtd/parent nav)           ;=> move to the parent element
(vtd/root nav)             ;=> move to the root element
```

We can also test navigators to distinguish elements from the entire document:

```clojure
(-> nav vtd/first-child vtd/element?)   ;=> true
(-> nav vtd/parent vtd/document?)       ;=> true
(-> nav vtd/first-child vtd/attribute?) ;=> false
```

As we are positioned on the `author` element, we might now want to collect the
text values of the `name` elements within it. We could do this using the
directional functions above but riveted provides a `children` function to do
this for us:

```clojure
(->> nav vtd/first-child vtd/next-sibling vtd/children (map vtd/text))
;=> ("Robert Paulson" "Joe Bloggs")

;; or if you prefer not to use the threading macro:
(map vtd/text (vtd/children (vtd/next-sibling (vtd/first-child nav))))
```

Note that `children`, along with `next-siblings` and `previous-siblings`,
returns a lazy sequence of matching elements. They also take an optional
second argument which allows you to specify an element name which will
restrict results further.

For example, if you wanted to return the `author` element directly from the
original navigator, you could ask for the first `author` child like so:

```clojure
(-> nav (vtd/first-child :author))
```

Or ask the root for all child `author` elements:

```clojure
(-> nav (vtd/children :author)) ;=> a sequence of all author child elements
```

You can also get the full text content of a mixed-content node with `text`
which would be perfect for our `abstract` element:

```clojure
(-> nav (vtd/first-child :abstract) vtd/text)
;=> "A great article all about things."
```

If you want to retrieve the raw XML contents of a node, you can use `fragment`
to do so:

```clojure
(-> nav (vtd/first-child :abstract) vtd/fragment)
;=> "A <i>great</i> article all about things."
```

### Traversing by element name

If we'd rather not navigate a document in terms of directions, riveted also
provides a way to traverse XML by element names with `select`.

To continue our example from above, if we wanted to pull the `title` text, we
could ask the navigator for all `title` elements (regardless of location) like
so:

```clojure
(vtd/select nav :title)
```

As this is a lazy sequence, we can ask for the text of the first item like so:

```clojure
(-> nav (vtd/select :title) first vtd/text) ;=> "Foo bar"
```

Similarly, we can ask for the text value of all `name` elements like so:

```clojure
(map vtd/text (vtd/select nav :name)) ;=> ("Robert Paulson" "Joe Bloggs")
```

Note that this will return `name` elements *anywhere* in the document but we
could restrict its search by moving the navigator, perhaps using some of the
direction functions from above:

```clojure
(map vtd/text (-> nav (vtd/first-child :author) (vtd/select :name)))
;=> ("Robert Paulson" "Joe Bloggs")
```

Or perhaps with `select` itself:

```clojure
(map vtd/text (-> nav (vtd/select :author) first (vtd/select :name)))
;=> ("Robert Paulson" "Joe Bloggs")
```

Finally, we can return a lazy sequence of *all* elements by simply using a
wildcard match:

```clojure
(vtd/select nav "*")
```

### Traversing by XPath

The last way to traverse a document is to use XPath 1.0 with the `search`
function. Note that this is only used to navigate to elements (so it's not
possible to directly return attribute values with an XPath expression).

For example, to select all `name` elements:

```clojure
(vtd/search nav "//name")
```

If you are expecting only one match then you can use the `at` function to
return only one result:

```clojure
(vtd/at nav "/article/title")
```

If accessing attributes via XPath, you can use `text` to return the value of
the attribute:

```clojure
(text (vtd/at nav "/article/@id"))
```

### Namespace support

If you wish to use namespace-aware features, you will need to enable namespace
support when creating the initial navigator like so:

```clojure
(def ns-nav (vtd/navigator (slurp "namespaced.xml") true))
```

You can then pass a prefix and URL when using `search` and `at` like so:

```clojure
(vtd/search ns-nav "//ns1:name" "ns1" "http://purl.org/dc/elements/1.1/")
```

### Flat view of tokens

If you need lower level access to the parsed document, you can exploit the
fact that navigators implement [Clojure's `Seqable`
interface](http://clojure.org/sequences) and can be traversed as a flat
sequence much like a list or vector:

```clojure
(first nav)  ;=> {:type :start-tag, :value "article"}
(second nav) ;=> {:type :start-tag, :value "title"}
(nth nav 2)  ;=> {:type :character-data, :value "Foo bar"}
(nth nav 4)  ;=> {:type :attribute-name, :value "id"}
(seq nav)    ;=> the full sequence of tokens

;; Return all comments from a document.
(filter (comp #{:comment} :type) nav)
```

This gives you access to *all* tokens in the document including XML
declarations, doctypes, comments, processing instructions, etc. However, it is
a very low level of abstraction and if you only care about navigating
elements, it might be better to use a cursor-based view instead.

### Mutable interface

riveted also provides a mutable interface to
[VTDNav](http://vtd-xml.sourceforge.net/javadoc/com/ximpleware/VTDNav.html)
(much like Clojure's [transient](http://clojure.org/transients) data
structures) for lower-memory usage (at the cost of immutability):

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

In order to mitigate the problems with mutable state, it might be best to use
the above functions much like you would `transient`; viz. within the confines
of a function like so:

```clojure
(defn title [nav]
  (-> (vtd/root nav)                    ; Create a new navigator to the root
      (vtd/first-child! :front)         ; for mutation.
      (vtd/first-child! :article-meta)
      (vtd/first-child! :title-group)
      (vtd/first-child! :article-title)
      vtd/text))
```

In this way, only one extra navigator is created.

## Acknowledgements

[Andrew Diamond's `clj-vtd-xml`](https://github.com/diamondap/clj-vtd-xml) and
[Tim Williams' gist](https://gist.github.com/willtim/822769) are existing
interfaces to VTD-XML from Clojure that were great sources of inspiration.

[Dave Ray's `seesaw`](https://github.com/daveray/seesaw) set the standard for
helpful docstrings.

Clojure's
[`core.clj`](https://github.com/clojure/clojure/blob/master/src/clj/clojure/core.clj)
provided fascinating reading, particularly regarding the use of `:inline`
metadata.

Thanks to [Heikki Hämäläinen](https://github.com/hjhamala) for contributing a
character encoding fix for Windows users.

Thanks to [Eugen Stan](https://github.com/ieugen) for suggesting that
`navigator` should also accept byte arrays as well as UTF-8 strings.

## License

Copyright © 2013-2022 Paul Mucur.

Distributed under the Eclipse Public License, the same as Clojure.
