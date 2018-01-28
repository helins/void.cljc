# Void

[![Clojars
Project](https://img.shields.io/clojars/v/dvlopt/void.svg)](https://clojars.org/dvlopt/void)

Clojure is about handling information.

Nil represents the absence of information.

This micro library proposes macros and functions for interacting with nil under
various circumstances.

## Usage

Read the [API](https://dvlopt.github.io/doc/void/).

For instance :

```clj
(require '[dvlopt.void :as void])


(void/assoc-some {}
                 ::a 42
                 ::b nil)
;; => {::a 42}


(def values
     {::opt-a :a
      ::opt-b :b})


(def opts
     {::opt-b :B})


(void/obtain ::opt-a
             opts
             values)
;; => :a

(void/obtain ::opt-b
             opts
             values)
;; => :B


(void/select [::opt-a
              ::opt-b
              ::opt-c]
             opts
             values)
;; => {::opt-a :a
;;     ::opt-b :B}

```

## License

Copyright © 2018 Adam Helinski

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
