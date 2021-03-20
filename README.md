# Void, about handling nil

[![Clojars
Project](https://img.shields.io/clojars/v/io.helins/void.svg)](https://clojars.org/io.helins/void)

[![Cljdoc](https://cljdoc.org/badge/io.helins/void)](https://cljdoc.org/d/io.helins/void)


Compatible with Clojurescript.

Nil is an information representing the absence of information.

This leads to puzzling shenanigans:

```clj
;; This is an empty collection, a perfectly normal phenomenon:

[]

;; It contains nothing.

;; This is a collection containing something that is nothing:

[nil]

;; Sometimes it is not what we want.

;; This is a map containing something that is nothing, nowhere:

{nil nil}

;; This is seldom what we want.
```

This small library proposes macros and functions for interacting with nil under
various circumstances. Most notably, it considers that nothing is nothing as the
following few examples demonstrate.


## Usage

Meditate and reflect on the full [API](https://cljdoc.org/d/io.helins/void).

Some Socratic excerpts :

```clj
(require '[helins.void :as void])


(= (void/assoc {:a 42}
                :a nil
                :b 42)
    
   {:a 42
    :b 42})


(= (void/update {:a 42
                 :b 42}
                :b
                (fn [x]
                  nil))

   {:a 42})


(= (void/dissoc-in {:a {:b {:c {:d 42}}
                        :e 42}}
                   [:a :b :c :d])

   {:a {:e 42}})


(= (void/update-in {:a {:b {:c {:d 24}}
                        :e 42}}
                   [:a :b :c :d]
                   (fn [x]
                     (when (= x
                              42)
                       x)))

   {:a {:e 42}})


(= (void/merge {:a 42
                :b 42}
               {:b nil
                :c 42})

   {:a 42
    :c 42})


(= (void/dmerge {:a {:b {:c {:d 42}}
                     :e 42}}
                {:a {:b {:c {:d nil}}
                     :f 42}})

   {:a {:e 42
        :f 42}})


(= (void/prune {:a 42
                :b {:c 42
                    :d nil
                    :e {:f {:g nil}}}})

   {:a 42
    :b {:c 42}})
```


## Running tests <a name="tests">

On the JVM, using [Kaocha](https://github.com/lambdaisland/kaocha):

```bash
$ ./bin/test/jvm/run
$ ./bin/test/jvm/watch
```
On NodeJS, using [Kaocha-CLJS](https://github.com/lambdaisland/kaocha-cljs):

```bash
$ ./bin/test/node/run
$ ./bin/test/node/watch
```

In the browser, using [Chui](https://github.com/lambdaisland/chui):
```
$ ./bin/test/browser/compile
# Then open ./resources/chui/index.html

# For testing an advanced build
$ ./bin/test/browser/advanced
```


## Development <a name="develop">

Starting in Clojure JVM mode, mentioning an additional deps alias (here, a local
setup of NREPL):
```bash
$ ./bin/dev/clojure :nrepl
```

Starting in CLJS mode using Shadow-CLJS:
```bash
$ ./bin/dev/cljs
# Then open ./resources/public/index.html
```


## License

Copyright © 2018 Adam Helinski

Licensed under the term of the Mozilla Public License 2.0, see LICENSE.
