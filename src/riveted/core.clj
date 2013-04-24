(ns riveted.core
  (:require [clojure.string :as s])
  (:import  [com.ximpleware VTDGen VTDNav AutoPilot TextIter]))

(defn navigator
  "Given an XML string, parse and return a new VTD navigator."
  [xml namespace-aware]
  (let [vg (doto (VTDGen.) (.setDoc (.getBytes xml)) (.parse namespace-aware))]
    (.getNav vg)))

(defn tag
  "Given a VTD navigator, return the tag name."
  [navigator]
  (let [index (.getCurrentIndex navigator)]
    (.toString navigator index)))

(defn attr
  "Return the value of the named attribute for the given navigator."
  [navigator attr-name]
  (let [index (.getAttrVal navigator (name attr-name))]
    (when (not= index -1)
      (.toNormalizedString navigator index))))

(defn fragment
  [navigator]
  (let [r (.getContentFragment navigator)]
    (.toString navigator (bit-and r 16rFFFFFF) (bit-shift-right r 32))))

(defn- navigate
  [navigator direction]
  (let [navigator' (.cloneNav navigator)]
    (when (.toElement navigator' direction)
      navigator')))

(defn next-sibling
  [navigator]
  (navigate navigator VTDNav/NEXT_SIBLING))

(defn parent
  [navigator]
  (navigate navigator VTDNav/PARENT))

(defn first-child
  [navigator]
  (navigate navigator VTDNav/FIRST_CHILD))

(defn siblings
  [navigator]
  (if-let [sibling (next-sibling navigator)]
    (cons sibling (lazy-seq (siblings sibling)))
    []))

(defn children
  [navigator]
  (if-let [child (first-child navigator)]
    (cons child (siblings child))
    []))

(defn- text-seq
  [text-iter]
  (let [index (.getNext text-iter)]
    (if (not= index -1)
      (cons index (lazy-seq (text-seq text-iter)))
      [])))

(defn- index->text
  [navigator index]
  (when (not= index -1)
    (.toNormalizedString navigator index)))

(defn- text-indices
  [navigator]
  (let [iter (doto (TextIter.) (.touch navigator))]
    (text-seq iter)))

(defn- text-children
  [navigator]
  (map (partial index->text navigator) (text-indices navigator)))

(defn- text-descendant-indices
  [navigator]
  (sort (concat (text-indices navigator) (lazy-seq (mapcat text-descendant-indices (children navigator))))))

(defn text-descendants
  [navigator]
  (map (partial index->text navigator) (text-descendant-indices navigator)))

(defn text
  "Return all descendent text content below the given navigator as one string."
  [navigator]
  (s/join " " (text-descendants navigator)))

(defn- xpath-seq
  [navigator autopilot]
  (let [index (.evalXPath autopilot)]
    (if-not (= index -1)
      (cons (.cloneNav navigator)
            (lazy-seq (xpath-seq navigator autopilot)))
      [])))

(defn search
  "Search for the given XPath in the navigator, returning all matching navigators."
  [navigator xpath]
  (let [navigator' (.cloneNav navigator)
        autopilot (doto (AutoPilot. navigator') (.selectXPath xpath))]
    (xpath-seq navigator' autopilot)))

(defn at
  "Search for the given XPath in the navigator, returning the first matching navigator."
  [navigator xpath]
  (first (search navigator xpath)))
