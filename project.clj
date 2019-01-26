(defproject refame-transform "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.5"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [org.clojure/core.async "0.2.391"]
                 [re-com "2.1.0"]
                 [re-pressed "0.3.0"]
                 [re-frisk-remote "0.5.5"]
                 [com.bhauman/rebel-readline-cljs "0.1.4"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-less "1.7.5"]
            [lein-re-frisk "0.5.8"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs/ui" "src/cljs/main" "src/cljs/common"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :less {:source-paths ["less"]
         :target-path  "resources/public/css"}

  :profiles
    {:dev
           {:dependencies [[binaryage/devtools "0.9.10"]
                           [day8.re-frame/re-frame-10x "0.3.3"]
                           [day8.re-frame/tracing "0.5.1"]
                           [figwheel-sidecar "0.5.10"]]
  
            :plugins      [[lein-figwheel "0.5.16"]]}
     :prod {:dependencies [[day8.re-frame/tracing-stubs "0.5.1"]]}}

  :cljsbuild
    {:builds
     [
      ;{:id           "dev"
      ;   :source-paths ["src/cljs"]
      ;   :figwheel     {:on-jsload "refame-transform.core/mount-root"}
      ;   :compiler     {:main                 refame-transform.core
      ;                  :output-to            "resources/public/js/compiled/app.js"
      ;                  :output-dir           "resources/public/js/compiled/out"
      ;                  :asset-path           "js/compiled/out"
      ;                  :source-map-timestamp true
      ;                  :preloads             [devtools.preload
      ;                                         day8.re-frame-10x.preload
      ;                                         re-frisk.preload]
      ;                  :closure-defines      {"re_frame.trace.trace_enabled_QMARK_"        true
      ;                                         "day8.re_frame.tracing.trace_enabled_QMARK_" true}
      ;                  :external-config      {:devtools/config {:features-to-install :all}}
      ;                  }}
  
      {:source-paths ["src/cljs/ui" "src/cljs/common"]
       :id           "ui-dev"
       :figwheel     {:on-jsload       "electro-note.core/mount-root"}
       :compiler     {:output-to       "resources/public/js/ui-core.js"
                      :output-dir      "resources/public/js/ui-out"
                      :source-map      true
                      :asset-path      "js/ui-out"
                      :optimizations   :none
                      :cache-analysis  true
                      :main            "electro_note.core"
                      :preloads        [devtools.preload
                                        day8.re-frame-10x.preload
                                        re-frisk.preload]
                      :closure-defines {"re_frame.trace.trace_enabled_QMARK_"        true
                                        "day8.re_frame.tracing.trace_enabled_QMARK_" true}
                      :external-config {:devtools/config {:features-to-install    [:formatters :hints]
                                                          :fn-symbol              "F"
                                                          :print-config-overrides true}}}}
      {:source-paths ["src/cljs/main" "src/cljs/common"]
       :id           "main-dev"
       :compiler     {:output-to      "resources/main.js"
                      :output-dir     "resources/public/js/main-dev"
                      :optimizations  :simple
                      :pretty-print   true
                      :cache-analysis true}}
  
  
      ;{:id           "min"
      ;   :source-paths ["src/cljs"]
      ;   :compiler     {:main            refame-transform.core
      ;                  :output-to       "resources/public/js/compiled/app.js"
      ;                  :optimizations   :advanced
      ;                  :closure-defines {goog.DEBUG false}
      ;                  :pretty-print    false}}
  
      ]}
  )
