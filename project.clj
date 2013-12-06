(defproject pisto "0.1-SNAPSHOT"
  :description "A simplistic formalization of the lifecycle of stateful systems. Removes a lot of the pain related to debugging a system of many simple services."
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :url "https://github.com/molst/pisto"
  :scm {:name "git" :url "https://github.com/molst/pisto"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]]}})
