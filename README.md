# riveted

A work-in-progress Clojure interface for parsing XML with
[VTD-XML](http://vtd-xml.sourceforge.net).

## Usage

```clojure
(ns foo
  (:require [riveted.core :as vtd]))

(def nav (navigator (slurp "foo.xml")))

(def title
  (search nav "/article/front/article-meta/title-group/article-title"))
; => returns another navigator that can be interrogated...

(tag title)
; => "article-title"

(text title)
; => "Some title"

(attr title :id)
; => "123"
```

## Acknowledgements

[Andrew Diamond's `clj-vtd-xml`](https://github.com/diamondap/clj-vtd-xml) and
[Tim William's gist](https://gist.github.com/willtim/822769) are existing
interfaces to VTD-XML from Clojure.

## License

Copyright © 2013 Paul Mucur.

Distributed under the Eclipse Public License, the same as Clojure.
