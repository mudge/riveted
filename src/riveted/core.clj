(ns ^{:doc "A Clojure library for the fast processing of XML with VTD-XML."
      :author "Paul Mucur"}
  riveted.core
  (:require [clojure.string :as s])
  (:import  [com.ximpleware VTDGen VTDNav AutoPilot TextIter]))

(set! *warn-on-reflection* true)

(defn- token-type-name
  "Private. Return a keyword representing the token type of the given
  VTDNav.

  Possible values are:

  * :start-tag
  * :end-tag
  * :attribute-name
  * :attribute-value
  * :namespace
  * :character-data
  * :comment
  * :processing-instruction-name
  * :processing-instruction-value
  * :declaration-attribute-name
  * :declaration-attribute-value
  * :cdata
  * :doctype"
  [^VTDNav nav index]
  (condp = (.getTokenType nav index)
    VTDNav/TOKEN_DOCUMENT       :document
    VTDNav/TOKEN_STARTING_TAG   :start-tag
    VTDNav/TOKEN_ENDING_TAG     :end-tag
    VTDNav/TOKEN_ATTR_NAME      :attribute-name
    VTDNav/TOKEN_ATTR_NS        :namespace
    VTDNav/TOKEN_ATTR_VAL       :attribute-value
    VTDNav/TOKEN_CHARACTER_DATA :character-data
    VTDNav/TOKEN_COMMENT        :comment
    VTDNav/TOKEN_PI_NAME        :processing-instruction-name
    VTDNav/TOKEN_PI_VAL         :processing-instruction-value
    VTDNav/TOKEN_DEC_ATTR_NAME  :declaration-attribute-name
    VTDNav/TOKEN_DEC_ATTR_VAL   :declaration-attribute-value
    VTDNav/TOKEN_CDATA_VAL      :cdata
    VTDNav/TOKEN_DTD_VAL        :doctype))

(defn- index-seq
  "Private. Return a lazy sequence of all tokens from the given VTDNav and
  index onwards.

  Tokens are represented as maps with a :type and :value entry."
  [^VTDNav nav index]
  (when (< index (.getTokenCount nav))
    (cons {:type (token-type-name nav index)
           :value (.toNormalizedString nav index)}
          (lazy-seq (index-seq nav (inc index))))))

;;; Wrapper type for the VTDNav class in order to implement Clojure's
;;; Sequential, Seqable and Counted interfaces.

(deftype Navigator [^VTDNav nav]
  clojure.lang.Sequential
  clojure.lang.Seqable
  (seq [this]
    (index-seq nav (.getCurrentIndex nav)))
  clojure.lang.Counted
  (count [this]
    (.getTokenCount nav)))

(defn- vtd-nav
  "Private. Return the VTDNav for a given Navigator.

  The use of vary-meta in the inline version of this function is in order to
  type hint the navigator and return type.

  See:
    http://stackoverflow.com/questions/7754429/clojure-defmacro-loses-metadata"
  {:inline (fn [navigator]
             (vary-meta `(.nav ~(vary-meta navigator assoc :tag `Navigator))
                        assoc :tag `VTDNav))}
  ^VTDNav [^Navigator navigator] (.nav navigator))

(defn- index
  "Private. Return the current index of the given navigator."
  {:inline (fn [navigator] `(.getCurrentIndex (vtd-nav ~navigator)))}
  [navigator]
  (.getCurrentIndex (vtd-nav navigator)))

(defn- clone
  "Private. Returns a new navigator cloned from the given one."
  {:inline (fn [navigator] `(Navigator. (.cloneNav (vtd-nav ~navigator))))}
  [navigator]
  (Navigator. (.cloneNav (vtd-nav navigator))))

(defn navigator
  "Return a VTD navigator for the given XML string with optional namespace
  support.  If called with only a string of XML, namespace support is disabled.

  Examples:

    ; Return a navigator for the given string with no namespace support.
    (navigator \"<root><foo>Bar</foo></root>\")

    ; Return a navigator for the given string with namespace support.
    (navigator \"<root xmlns:ns=\\\"http://example.com/ns\\\"><foo>Bar</foo></root>\" true)"
  ([^String xml] (navigator xml false))
  ([^String xml namespace-aware]
   (let [vg (doto (VTDGen.) (.setDoc (.getBytes xml))
                            (.parse namespace-aware))]
     (Navigator. (.getNav vg)))))

(defn tag
  "Return the tag name for the element under the given VTD navigator as a
  string.

  Examples:

    (tag (root nav))
    ;=> \"root\""
  [navigator]
  (.toString (vtd-nav navigator) (index navigator)))

(defn- index->text
  "Private. Returns the text value of a node identified by the given index in
  the given navigator."
  {:inline (fn [navigator index]
             `(when-not (= ~index -1)
                (.toNormalizedString (vtd-nav ~navigator) ~index)))}
  [navigator index]
  (when-not (= index -1)
    (.toNormalizedString (vtd-nav navigator) index)))

(defn attr
  "Return the value of the named attribute for the given navigator.
  Attributes can be specified with either a keyword or string name.

  Examples:

    (attr (root nav) :lang)
    ;=> \"en\""
  [navigator attr-name]
  (let [index (.getAttrVal (vtd-nav navigator) (name attr-name))]
    (index->text navigator index)))

(defn attr?
  "Test whether the given attribute exists on the current element.
  Attributes can be specified with either a keyword or string name.

  Examples:

    (attr? (root nav) :lang)
    ;=> true"
  [navigator attr-name]
  (.hasAttr (vtd-nav navigator) (name attr-name)))

(defn fragment
  "Return a string XML fragment for all nodes under the given navigator.

  Examples:

    (fragment nav)
    ;=> \"<b>Some</b> XML as a raw <i>string</i>\""
  [navigator]
  (let [nav (vtd-nav navigator)
        r (.getContentFragment nav)]
    (.toString nav (bit-and r 16rFFFFFF) (bit-shift-right r 32))))

;;; Transient interface for navigation.

(defn- navigate!
  "Private. Low level interface to move the given navigator in the given
  direction (optionally restricting moving to the given element type), mutating
  it in place.

  Note that this changes the internal state of the navigator (thereby suffering
  from the usual problems of mutability including concurrency woes) but saves
  duplicating the navigator's state on every move.

  Direction should be one of the standard VTDNav constants, namely:

  * VTDNav/ROOT;
  * VTDNav/FIRST_CHILD;
  * VTDNav/LAST_CHILD;
  * VTDNav/NEXT_SIBLING;
  * VTDNav/PREV_SIBLING;
  * VTDNav/PARENT.

  Examples:

    ; Move the navigator to the document root.
    (navigate! nav VTDNav/ROOT)

    ; Move the navigator to the first p child tag.
    (navigate! nav VTDNav/FIRST_CHILD :p)

  See:
    http://vtd-xml.sourceforge.net/javadoc/com/ximpleware/VTDNav.html"
  ([navigator direction]
    {:pre [(>= direction 0) (<= direction 5)]}
    (when (.toElement (vtd-nav navigator) direction)
      navigator))
  ([navigator direction element]
    {:pre [(>= direction 0) (<= direction 5)]}
    (when (.toElement (vtd-nav navigator) direction (name element))
      navigator)))

(defn root!
  "Move the given navigator to the document root, mutating it in place."
  [navigator]
  (navigate! navigator VTDNav/ROOT))

(defn parent!
  "Move the given navigator to the current element's parent element, mutating
  it in place."
  [navigator]
  (navigate! navigator VTDNav/PARENT))

(defn next-sibling!
  "Move the given navigator to the current element's next sibling element
  (restricted by an optional element type), mutating it in place.

  Examples:

    ; Move nav to the next sibling element.
    (next-sibling! nav)

    ; Move nav to the next sibling b element.
    (next-sibling! nav :b)"
  ([navigator]         (navigate! navigator VTDNav/NEXT_SIBLING))
  ([navigator element] (navigate! navigator VTDNav/NEXT_SIBLING element)))

(defn previous-sibling!
  "Move the given navigator to the current element's previous sibling element
  (restricted by an optional element type), mutating it in place.

  Examples:

    ; Move nav to the previous sibling element.
    (previous-sibling! nav)

    ; Move nav to the previous sibling b element.
    (previous-sibling! nav :b)"
  ([navigator]         (navigate! navigator VTDNav/PREV_SIBLING))
  ([navigator element] (navigate! navigator VTDNav/PREV_SIBLING element)))

(defn first-child!
  "Move the given navigator to the current element's first child element
  (restricted by an optional element type), mutating it in place.

  Examples:

    ; Move nav to the first child element.
    (first-child! nav)

    ; Move nav to the first child b element.
    (first-child! nav :b)"
  ([navigator]         (navigate! navigator VTDNav/FIRST_CHILD))
  ([navigator element] (navigate! navigator VTDNav/FIRST_CHILD element)))

(defn last-child!
  "Move the given navigator to the current element's last child element
  (restricted by an optional element type), mutating it in place.

  Examples:

    ; Move nav to the last child element.
    (last-child! nav)

    ; Move nav to the last child b element.
    (last-child! nav :b)"
  ([navigator]         (navigate! navigator VTDNav/LAST_CHILD))
  ([navigator element] (navigate! navigator VTDNav/LAST_CHILD element)))

;;; Immutable interface to navigation.

(defn- navigate
  "Private. Low level interface to return a new navigator based on moving the
  given one in the given direction (optionally restricting movement to the given
  element type). Note that this *does not* mutate the existing navigator unlike
  (riveted.core/navigate!).

  This relies on cloning the given navigator before moving and therefore will
  use more memory than (riveted.core/navigate!) but provides the benefits of an
  immutable interface.

  Direction should be one of the standard VTDNav constants, namely:

  * VTDNav/ROOT;
  * VTDNav/FIRST_CHILD;
  * VTDNav/LAST_CHILD;
  * VTDNav/NEXT_SIBLING;
  * VTDNav/PREV_SIBLING;
  * VTDNav/PARENT.

  Examples:

    ; Return a new navigator pointing at the document root.
    (navigate nav VTDNav/ROOT)

    ; Return a new navigator pointing at the first p child tag.
    (navigate nav VTDNav/FIRST_CHILD :p)

  See:
    http://vtd-xml.sourceforge.net/javadoc/com/ximpleware/VTDNav.html
    (riveted.core/navigate!)"
  ([^VTDNav navigator direction]
    (let [navigator' (clone navigator)]
      (navigate! navigator' direction)))
  ([^VTDNav navigator direction element]
    (let [navigator' (clone navigator)]
      (navigate! navigator' direction element))))

(defn root
  "Return a new navigator pointing to the document root."
  [navigator]
  (navigate navigator VTDNav/ROOT))

(defn parent
  "Return a new navigator pointing to the parent element of the given
  navigator."
  [navigator]
  (navigate navigator VTDNav/PARENT))

(defn next-sibling
  "Return a new navigator pointing to the current element's next sibling element
  (restricted by an optional element type).

  Examples:

    ; Return a new navigator pointing to the next sibling element of nav.
    (next-sibling nav)

    ; Return a new navigator pointing to the next sibling b element of nav.
    (next-sibling nav :b)"
  ([navigator]         (navigate navigator VTDNav/NEXT_SIBLING))
  ([navigator element] (navigate navigator VTDNav/NEXT_SIBLING element)))

(defn previous-sibling
  "Return a new navigator pointing to the current element's previous sibling
  element (restricted by an optional element type).

  Examples:

    ; Return a new navigator pointing to the previous sibling element of nav.
    (previous-sibling nav)

    ; Return a new navigator pointing to the previous sibling b element of nav.
    (previous-sibling nav :b)"
  ([navigator]         (navigate navigator VTDNav/PREV_SIBLING))
  ([navigator element] (navigate navigator VTDNav/PREV_SIBLING element)))

(defn first-child
  "Return a new navigator pointing to the current element's first child element
  (restricted by an optional element type).

  Examples:

    ; Return a new navigator pointing to the first child element of nav.
    (first-child nav)

    ; Return a new navigator pointing to the first child b element of nav.
    (first-child nav :b)"
  ([navigator]         (navigate navigator VTDNav/FIRST_CHILD))
  ([navigator element] (navigate navigator VTDNav/FIRST_CHILD element)))

(defn last-child
  "Return a new navigator pointing to the current element's last child element
  (restricted by an optional element type).

  Examples:

    ; Return a new navigator pointing to the last child element of nav.
    (last-child nav)

    ; Return a new navigator pointing to the last child b element of nav.
    (last-child nav :b)"
  ([navigator]         (navigate navigator VTDNav/LAST_CHILD))
  ([navigator element] (navigate navigator VTDNav/LAST_CHILD element)))

(defn next-siblings
  "Return a lazy sequence of navigators representing all siblings next to the
  given navigator (optionally restricted by a given element type).

  Examples:

    ; Return navigators for every next sibling element to nav.
    (next-siblings nav)

    ; Return navigators for every next sibling p element to nav.
    (next-siblings nav :p)"
  ([navigator]
    (when-let [sibling (next-sibling navigator)]
      (cons sibling (lazy-seq (next-siblings sibling)))))
  ([navigator element]
    (when-let [sibling (next-sibling navigator element)]
      (cons sibling (lazy-seq (next-siblings sibling element))))))

(defn previous-siblings
  "Return a lazy sequence of navigators representing all siblings previous to
  the given navigator (optionally restricted by a given element type).

  Note that this is lazily evaluated right-to-left so the final sequence will
  be in reverse order to the actual nodes in the document.

  Examples:

    ; Return navigators for every previous sibling element to nav.
    (previous-siblings nav)

    ; Return navigators for every previous sibling p element to nav.
    (previous-siblings nav :p)"
  ([navigator]
    (when-let [sibling (previous-sibling navigator)]
      (cons sibling (lazy-seq (previous-siblings sibling)))))
  ([navigator element]
    (when-let [sibling (previous-sibling navigator element)]
      (cons sibling (lazy-seq (previous-siblings sibling element))))))

(defn siblings
  "Return navigators for all siblings to the given navigator (optionally
  restricted by a given element type).

  Note that this is not lazy in order to preserve the correct order of nodes
  and previous siblings need to be fully realised for sorting.

  Examples:

    ; Return navigators for all siblings to nav.
    (siblings nav)

    ; Return navigators for all sibling p elements to nav.
    (siblings nav :p)"
  {:inline (fn [navigator & args]
             `(let [left# (reverse (previous-siblings ~navigator ~@args))
                    right# (next-siblings ~navigator ~@args)]
                (when (or (seq left#) (seq right#))
                  (concat left# right#))))
   :inline-arities #{1 2}}
  ([navigator]
    (let [left  (reverse (previous-siblings navigator))
          right (next-siblings navigator)]
      (when (or (seq left) (seq right))
        (concat left right))))
  ([navigator element]
    (let [left  (reverse (previous-siblings navigator element))
          right (next-siblings navigator element)]
      (when (or (seq left) (seq right))
        (concat left right)))))

(defn children
  "Return a lazy sequence of navigators for all child nodes of the given
  navigator (optionally restricted by a given element type).

  Examples:

    ; Return navigators for all children of nav.
    (children nav)

    ; Return navigators for all child p elements of nav.
    (children nav :p)"
  {:inline (fn [navigator & args]
             `(when-let [child# (first-child ~navigator ~@args)]
                (cons child# (lazy-seq (next-siblings child# ~@args)))))
   :inline-arities #{1 2}}
  ([navigator]
    (when-let [child (first-child navigator)]
      (cons child (lazy-seq (next-siblings child)))))
  ([navigator element]
    (when-let [child (first-child navigator element)]
      (cons child (lazy-seq (next-siblings child element))))))

(defn- text-seq
  "Private. Returns a lazy sequence of all text nodes for a given TextIter."
  [^TextIter text-iter]
  (let [index (.getNext text-iter)]
    (when-not (= index -1)
      (cons index (lazy-seq (text-seq text-iter))))))

(defn- text-indices
  "Private. Creates a TextIter for the given navigator and returns a sequence of
  indices for all text nodes associated with it."
  [navigator]
  (let [iter (doto (TextIter.) (.touch (vtd-nav navigator)))]
    (text-seq iter)))

(defn- text-descendant-indices
  "Private. Returns an ordered sequence of the indices of all text nodes that
  are descendants of the given navigator."
  [navigator]
  (sort (concat (text-indices navigator)
                (mapcat text-descendant-indices (children navigator)))))

(defn- text-descendants
  "Private. Returns a sequence of all text descending from the given navigator."
  [navigator]
  (map (partial index->text navigator) (text-descendant-indices navigator)))

(defn text
  "Return all descendant text content below the given navigator as one string.
  This means both the value of a simple text node and also the resulting text
  value of a mixed content node such as <p><b>Foo</b> bar</p>.

  Examples:

    ; Returns \"Foo\" given nav points to <p>Foo</p>
    (text nav)

    ; Returns \"Foo bar\" given nav points to <p><b>Foo</b> bar</p>
    (text nav)"
  [navigator]
  (when-let [texts (seq (text-descendants navigator))]
    (s/join " " texts)))

(defn- token-type
  "Private. Returns the token type of the given navigator."
  ([navigator]       (token-type navigator (index navigator)))
  ([navigator index] (.getTokenType (vtd-nav navigator) index)))

(defn element?
  "Tests whether the given navigator is currently positioned on an element."
  [navigator]
  (= VTDNav/TOKEN_STARTING_TAG (token-type navigator)))

(defn document?
  "Tests whether the given navigator is currently positioned the document."
  [navigator]
  (= VTDNav/TOKEN_DOCUMENT (token-type navigator)))

(defn- xpath-seq
  "Private. Returns a lazy sequence of navigators exhaustively evaluating XPath
  with the given navigator and AutoPilot."
  [navigator ^AutoPilot autopilot]
  (let [index (.evalXPath autopilot)]
    (when-not (= index -1)
      (cons (clone navigator)
            (lazy-seq (xpath-seq navigator autopilot))))))

(defn search
  "Search for the given XPath in the navigator, returning a lazy sequence of all
  matching navigators. If used with a namespace aware navigator, also takes
  a namespace prefix and URL for use in the XPath.

  Examples:

    ; Returns navigators for all matching elements.
    (search nav \"/article/title\")

    ; Returns navigators for all matching elements providing ns-nav is
    ; namespace aware.
    (search ns-nav \"//ns1:title\" \"ns1\" \"http://example.com/ns\")"
  ([navigator xpath]
    (let [navigator' (clone navigator)
          autopilot (doto (AutoPilot. (vtd-nav navigator'))
                          (.selectXPath xpath))]
      (xpath-seq navigator' autopilot)))
  ([navigator xpath prefix url]
    (let [navigator' (clone navigator)
          autopilot (doto (AutoPilot. (vtd-nav navigator'))
                          (.declareXPathNameSpace prefix url)
                          (.selectXPath xpath))]
      (xpath-seq navigator' autopilot))))

(defn- select-seq
  "Private. Returns a lazy sequence of navigators exhaustively iterating through
  nodes with the given navigator and AutoPilot."
  [navigator ^AutoPilot autopilot]
  (when (.iterate autopilot)
    (cons (clone navigator)
          (lazy-seq (select-seq navigator autopilot)))))

(defn select
  "Return a lazy sequence of navigators matching the given element name, * can
  be used to match all elements.

  Examples:

    ; Returns navigators for each element in nav.
    (select nav \"*\")

    ; Returns navigators for all b elements in nav.
    (select nav \"b\")"
  [navigator element]
  (let [navigator' (clone navigator)
        autopilot (doto (AutoPilot. (vtd-nav navigator'))
                        (.selectElement (name element)))]
    (select-seq navigator' autopilot)))

(defn at
  "Search for the given XPath in the navigator, returning the first matching
  navigator. If used with a namespace aware navigator, also takes a namespace
  prefix and URL for use in the XPath.

  Examples:

    ; Returns a single navigator for the first matching element.
    (at nav \"/article/title\")

    ; Returns a single navigator for the first matching element providing
    ; ns-nav is namespace aware.
    (at ns-nav \"//ns1:title\" \"ns1\" \"http://example.com/ns\")"
  {:inline (fn [& args] `(first (search ~@args)))
   :inline-arities #{2 4}}
  ([navigator xpath]            (first (search navigator xpath)))
  ([navigator xpath prefix url] (first (search navigator xpath prefix url))))

