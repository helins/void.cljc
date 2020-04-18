# Void

[![Clojars
Project](https://img.shields.io/clojars/v/dvlopt/void.svg)](https://clojars.org/dvlopt/void)

[![cljdoc badge](https://cljdoc.org/badge/dvlopt/void)](https://cljdoc.org/d/dvlopt/void)


Nil is an information representing the absence of information.

This leads to puzzling shenanigans:

```clj
;; This is an empty collection, a perfectly normal phenomenon:

[]

;; It contains nothing.

;; This is collection containing something that is nothing:

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


(void/assoc {}
            :a 42
            :b nil)

;; => {:a 42}


(void/update {:a 42
              :b 42}
             :b
             (fn [x]
               nil))

;; => {:a 42}


(void/dissoc-in {:a {:b {:c {:d 42}}
                     :e 42}}
                [:a :b :c :d])

;; => {:a {:e 42}}




(void/merge {:a 42
             :b 42}
            {:b nil
             :c 42})

;; => {:a 42
;;     :c 42}


(void/dmerge {:a {:b {:c {:d 42}}
                  :e 42}}
             {:a {:b {:c {:d nil}}
                  :f 42}})

;; => {:a {:e 42
;;         :f 42}}
```

Run tests:
```
$ ./bin/kaocha
```

## License

Copyright © 2018 Adam Helinski

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
