# Void

[![Clojars
Project](https://img.shields.io/clojars/v/dvlopt/void.svg)](https://clojars.org/dvlopt/void)

[![cljdoc badge](https://cljdoc.org/badge/dvlopt/void)](https://cljdoc.org/d/dvlopt/void)


[![Clojars Downloads](https://img.shields.io/clojars/dt/dvlopt/void?color=blue&style=flat-square)](https://clojars.org/dvlopt/void)

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

Meditate and reflect on the full [API](https://cljdoc.org/d/dvlopt/void).

Some Socratic excerpts :

```clj
(require '[dvlopt.void :as void])


(void/assoc {:a 42}
            :a nil
            :b 42)

{:a 42
 :b 42}


(void/update {:a 42
              :b 42}
             :b
             (fn [x]
               nil))

{:a 42}


(void/dissoc-in {:a {:b {:c {:d 42}}
                     :e 42}}
                [:a :b :c :d])

{:a {:e 42}}


(void/update-in {:a {:b {:c {:d 24}}
                     :e 42}}
                [:a :b :c :d]
                (fn [x]
                  (when (= x
                           42)
                    x)))

{:a {:e 42}}


(void/merge {:a 42
             :b 42}
            {:b nil
             :c 42})

{:a 42
 :c 42}


(void/dmerge {:a {:b {:c {:d 42}}
                  :e 42}}
             {:a {:b {:c {:d nil}}
                  :f 42}})

{:a {:e 42
     :f 42}}


(void/prune {:a 42
             :b {:c 42
                 :d nil
                 :e {:f {:g nil}}}})

{:a 42
 :b {:c 42}}
```

## Run tests

Run all tests (JVM and JS based ones):

```bash
$ ./bin/kaocha
```

For Clojure only:

```bash
$ ./bin/kaocha jvm
```

For Clojurescript on NodeJS, `ws` must be installed:
```bash
$ npm i ws
```
Then:
```
$ ./bin/kaocha node
```

For Clojurescript in the browser (which might need to be already running):
```bash
$ ./bin/kaocha browser
```


## License

Copyright © 2018 Adam Helinski

Licensed under the term of the Mozilla Public License 2.0, see LICENSE.
