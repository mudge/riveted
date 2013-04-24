# riveted [![Build Status](https://travis-ci.org/mudge/riveted.png?branch=master)](https://travis-ci.org/mudge/riveted)

A work-in-progress Clojure interface for parsing XML with
[VTD-XML](http://vtd-xml.sourceforge.net).

## Usage

```clojure
(ns foo
  (:require [riveted.core :as vtd]))

(def nav (vtd/navigator (slurp "foo.xml")))

(def title
  (vtd/at nav "/article/front/article-meta/title-group/article-title"))
;=> returns another navigator that can be interrogated...

(vtd/tag title)
;=> "article-title"

(vtd/text title)
;=> "Some title"

(vtd/attr title :id)
;=> "123"

(def bold-words
  (vtd/search nav "//b"))
;=> returns a lazy sequence of navigators for each matching element
```

## Acknowledgements

[Andrew Diamond's `clj-vtd-xml`](https://github.com/diamondap/clj-vtd-xml) and
[Tim William's gist](https://gist.github.com/willtim/822769) are existing
interfaces to VTD-XML from Clojure that were great sources of inspiration.

## License

Copyright © 2013 Paul Mucur.

Distributed under the Eclipse Public License, the same as Clojure.
