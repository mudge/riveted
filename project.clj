(defproject riveted "0.1.1"
  :description "A Clojure library for the fast processing of XML with VTD-XML."
  :url "https://github.com/mudge/riveted"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.ximpleware/vtd-xml "2.13"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.2.1"]
                             [lein-codox "0.10.3"]]}
             :1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5.1 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}}
  :aliases { "all" ["with-profile" "dev,1.3:dev,1.4:dev,1.5.1:dev,1.6:dev,1.7:dev,1.8"]}
  :codox {:src-dir-uri "https://github.com/mudge/riveted/blob/master"
          :src-linenum-anchor-prefix "L"})
