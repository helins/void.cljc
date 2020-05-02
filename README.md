# Ranked trees

[![Clojars
Project](https://img.shields.io/clojars/v/dvlopt/rktree.svg)](https://clojars.org/dvlopt/rktree)

[![cljdoc badge](https://cljdoc.org/badge/dvlopt/rktree)](https://cljdoc.org/d/dvlopt/rktree)

Compatible with Clojurescript.


A `ranked` tree is a peculiar but interesting data structure. It is a form of
nested maps where leaves are located both in time and space. It comes in handy
for problems that needs some form of prioritization.

More specifically, it is a complex of nested maps where former levels are sorted
and latter levels are unsorted.

Feel free to clone this repo, the examples below are in
[/dev/user.clj](dev/user.clj):

```sh
clj -A:dev:test

# And your favorite REPL
```

Following this definition, the following qualifies as a `ranked tree`:

```clojure
(def my-tree
     (sorted-map 0 (sorted-map 1 {:a {:b 'leaf-1}})
                 5 {:a {:d {:e 'leaf-2}}}))
```

Each leaf has two paths, one "horizontal" and one "vertical" (borrowing
vocabulary from the litterature). We shall rather talk - respectively - about
`ranks` providing a notion of time and a `path` providing a notion of space.

Thus, `'leaf-1` is said to be located at `[:a :b]` and ranked at `[0 1]`.
Similarly, `'leaf-2` is said to be located at `[:c :d :e]` and ranked at `[5]`.
We specify both the `ranks` and the `path` when we want to `get` something out
of this tree:

```clojure
(require '[dvlopt.rktree :as rktree])

(= 'leaf-1
   (rktree/get my-tree
               [0 1]
               [:a :b]))
```

Ranks provid prioritization, a lower rank meaning a higher priority, 0 being the
highest priority. When we `pop` the tree, we receive whatever resides at the
ranks with the highest priority. More precisely, we receive `[popped-tree ranks
unsorted-node]`:

```clojure
(= (rktree/pop my-tree)

   [(sorted-map 5 {:a {:d {:e 'leaf-2}}})
    [0 1]
    {:a {:b 'leaf-1}}])
```

Interestingly, as you might have noticed, different leaves can have ranks of
different length. This library handles that automagically. Remember we already
have `'leaf-1` located at `[:a :b]` and ranked at `[0 1]`. What if we `assoc`
something past those ranks?

```Clojure
(def my-tree-2
     (rktree/assoc my-tree
                   [0 1 0 0 0 5]
                   [:possible?]
                   true))

;; Notice that 'leaf has been re-prioritized from [0 1] to [0 1 0 0 0 0].
;; Order is actuall ymaintained as before, but we can account for the new
;; addition above.

(= 'leaf-1
   (rktree/get my-tree-2
               [0 1 0 0 0 0]
               [:a :b]))

;; But notice that we can still use the original ranks!

(= 'leaf-1
   (rktree/get my-tree-2
               [0 1]
               [:a :b]))
```

We have discovered a few recognizable functions such as `assoc` and `get`. The
[API](https://cljdoc.org/d/dvlopt/rktree) provide other ones (`dissoc`,
`update`, and friends), all acting on this idea of having `ranks` and a `path`.


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

Copyright © 2020 Adam Helinski

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
