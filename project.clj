(defproject kephale/clooj "0.4.4-SNAPSHOT"
  :description "clooj, a small IDE for clojure"
  :url "https://github.com/arthuredelstein/clooj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main clooj.core
  :pedantic :warn
  :dependencies [[org.clojure/clojure "1.5.1"]
                 ;[clj-inspector "0.0.12"]                 
                 [slamhound "1.2.0"]
                 ;[com.cemerick/pomegranate "0.0.11"]
                 [com.cemerick/pomegranate "0.2.0"]
                 [com.fifesoft/rsyntaxtextarea "2.0.2"]
                 [org.clojure/tools.nrepl "0.2.2"]
                 [leiningen-core "2.3.2"]
                 ;; From clj-inspector
                 [org.clojure/data.zip "0.1.1"]
                 
                 [leiningen "2.3.2"]
                 [lein-checkouts "1.1.0"]
                 ])
