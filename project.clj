(defproject tbot "0.1.0-SNAPSHOT"
  :description "Telegram bot for Gamble"
  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "1.3.610"]
                 [org.immutant/web    "2.1.10"]
                 [ring/ring-core      "1.8.2"]
                 [ring/ring-json      "0.5.0"]
                 [environ             "1.1.0"]
                 [morse               "0.2.4"]
                 [clj-pg              "0.0.3"]
                 [metosin/reitit-ring "0.3.10"]
                 [im.chit/cronj "1.4.4"]
                 [cheshire "5.10.0"]
                 [clj-http "3.9.1"]

                 [clj-commons/clj-yaml "0.7.0"]
                 [org.clojure/data.codec    "0.1.1"]
                 [json-schema "0.2.0-RC11"]
                 [com.draines/postal        "2.0.3"]]

  :plugins [[lein-environ "1.1.0"]]

  :main ^:skip-aot tbot.core
  :target-path "target/%s"

  :profiles {:uberjar {:aot :all}})
