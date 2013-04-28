(defproject riveted "0.0.4"
  :description "A Clojure interface for parsing XML with VTD-XML."
  :url "https://github.com/mudge/riveted"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.ximpleware/vtd-xml "2.11"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]
                   :plugins [[lein-midje "3.0.1"]]}})
