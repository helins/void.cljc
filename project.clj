(defproject dvlopt/void
            "0.0.0"

  :description "About void and the absence of information"
  :url         "https://github.com/dvlopt/void"
  :license     {:name "Eclipse Public License"
                :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles    {:dev {:source-paths ["dev"]
                      :main         user
                      :dependencies [[org.clojure/clojure    "1.9.0"]
                                     [org.clojure/test.check "0.10.0-alpha2"]
                                     [org.clojure/core.match "0.3.0-alpha5"]
                                     [criterium              "0.4.4"]]
                      :plugins      [[venantius/ultra "0.5.2"]
                                     [lein-codox      "0.10.3"]]
                      :codox        {:output-path  "doc/auto"
                                     :source-paths ["src"]}
                      :global-vars  {*warn-on-reflection* true}}})
