(ns foo
  (:require [riveted.core :as vtd]))

;; https://github.com/mudge/riveted#usage
(comment
  (def nav (vtd/navigator (slurp "resources/foo.xml")))

;; Navigating by direction and returning text content.
  (-> nav vtd/first-child vtd/next-sibling vtd/text)
   ;=> "Foo"

;; Navigating by direction, restricted by element and returning attribute
;; value.
  (-> nav (vtd/first-child :p) (vtd/attr :id))
   ;=> "42"

;; Return the tag names of all children elements.
  (->> nav vtd/children (map vtd/tag))
   ;=> ("p" "a" "b")

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
  )

(comment
;; https://github.com/mudge/riveted#mutable-interface

;; Create an initial navigator as per usual.
  (def nav (vtd/navigator "<root><a>Foo</a><b>Bar</b></root>"))

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
  (vtd/root! nav))