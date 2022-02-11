(defproject riveted "0.1.1"
  :description "A Clojure library for the fast processing of XML with VTD-XML."
  :url "https://github.com/mudge/riveted"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.ximpleware/vtd-xml "2.13"]]
  :profiles {:dev {:dependencies [[midje "1.10.5"]]
                   :plugins [[lein-midje "3.2.2"]
                             [lein-codox "0.10.8"]]}
             :1.3 {:dependencies [[midje "1.6.3"] [org.clojure/clojure "1.3.0"]]}
             :1.4 {:dependencies [[midje "1.6.3"] [org.clojure/clojure "1.4.0"]]}
             :1.5.1 {:dependencies [[midje "1.6.3"] [org.clojure/clojure "1.5.1"]]}
             :1.6 {:dependencies [[midje "1.6.3"] [org.clojure/clojure "1.6.0"]]}
             :1.7 {:dependencies [[midje "1.10.5"] [org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[midje "1.10.5"] [org.clojure/clojure "1.8.0"]]}
             :1.9 {:dependencies [[midje "1.10.5"] [org.clojure/clojure "1.9.0"]]}
             :1.10.0 {:dependencies [[midje "1.10.5"] [org.clojure/clojure "1.10.0"]]}
             :1.10.1 {:dependencies [[midje "1.10.5"] [org.clojure/clojure "1.10.1"]]}
             :1.10.2 {:dependencies [[midje "1.10.5"] [org.clojure/clojure "1.10.2"]]}
             :1.10.3 {:dependencies [[midje "1.10.5"] [org.clojure/clojure "1.10.3"]]}}
  :aliases {"all" ["with-profile" "dev,1.3:dev,1.4:dev,1.5.1:dev,1.6:dev,1.7:dev,1.8:dev,1.9:dev,1.10.0:dev,1.10.1:dev,1.10.2:dev,1.10.3"]}
  :codox {:src-dir-uri "https://github.com/mudge/riveted/blob/master"
          :src-linenum-anchor-prefix "L"})
