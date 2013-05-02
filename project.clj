(defproject riveted "0.0.7"
  :description "A Clojure library for the fast processing of XML with VTD-XML."
  :url "https://github.com/mudge/riveted"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [com.ximpleware/vtd-xml "2.11"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]
                   :plugins [[lein-midje "3.0.1"]
                             [codox "0.6.4"]]}
             :1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5.0 {:dependencies [[org.clojure/clojure "1.5.0"]]}
             :1.5.1 {:dependencies [[org.clojure/clojure "1.5.1"]]}}
  :codox {:src-dir-uri "https://github.com/mudge/riveted/blob/master"
          :src-linenum-anchor-prefix "L"})
