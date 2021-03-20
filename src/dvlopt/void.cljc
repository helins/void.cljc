;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at https://mozilla.org/MPL/2.0/.


(ns dvlopt.void

  "Macros and functions for handling nil under various circumstances." 

  {:author "Adam Helinski"}

  (:require [clojure.core :as clj])
  #?(:cljs (:require-macros [dvlopt.void]))

  ;; <!> Code is confusing if one does not remember this.
  ;;
  (:refer-clojure :exclude [#?(:cljs -assoc)
                            assoc
                            assoc-in
                            merge
                            merge-with
                            update
                            update-in]))




;;;;;;;;;; Gathering all declarations


(declare dmerge-with*
         merge-with*
         prune)




;;;;;;;;;; API


(defmacro alt

  "Selects the first non-nil value.

   Being a macro, arguments are not evaluated until needed.
  
   ```clojure
   (alt nil
		false
        42)

   false
   ```"

  ([]

   nil)


  ([x]

   x)


  ([x & xs]

   `(if-some [x'# ~x]
      x'#
      (alt ~@xs))))




(defn- -assoc

  ;; Used by `void/assoc` and `assoc-strict`.

  ([on-nil hmap k v]

   (if (nil? v)
     (on-nil hmap
             k)
     (clj/assoc hmap
                k
                v)))


  ([on-nil hmap k v kvs]

   (let [hmap-2 (-assoc on-nil
                        hmap
                        k
                        v)]
     (if (seq kvs)
       (recur on-nil
              hmap-2
              (first kvs)
              (second kvs)
              (nnext kvs))
       hmap-2))))



(defn- -on-nil-identity

  ;; Used by `void/assoc`.

  [hmap _k]

  hmap)



(defn assoc

  "Behaves like standard `assoc` but associates `v` only if it is not nil.
  
   ```clojure
   (assoc {:a 42}
          :a nil
          :b 42)

   {:a 42
    :b 42}
   ```"

  ([hmap k v]

   (-assoc -on-nil-identity
           hmap
           k
           v))


  ([hmap k v & kvs]

   (-assoc -on-nil-identity
           hmap
           k
           v
           kvs)))




(defn assoc-in

  "Behaves like standard `assoc-in` but associates `v` only if it is not nil.

   See also [[assoc]]."

  [hmap path v]

  (if (and (seq path)
           (some? v))
    (clj/assoc-in hmap
                  path
                  v)
    hmap))




(defn assoc-strict

  "Similar to this namespace's version of [[assoc]] but if `v` is nil, not only is it not associated, the
   involved key is actually removed from the map."

  ([hmap k v]

   (-assoc dissoc
           hmap
           k
           v))


  ([hmap k v & kvs]

   (-assoc dissoc
           hmap
           k
           v
           kvs)))




(defmacro call

  "Calls `f` with the given arguments only if `f` is not nil.
  
   Being a macro, the arguments are not evaluated until they are needed."

  [f & args]

  (when (some? f)
    `(when-some [f'# ~f]
       (f'# ~@args))))





(defn- -dissoc-in

  ;; Cf. `dissoc-in`

  [hmap [k & ks]]

  (if (seq ks)
    (let [v (get hmap
                 k)]
      (if (map? v)
        (assoc-strict hmap
                      k
                      (not-empty (-dissoc-in v
                                             ks)))
        hmap))
    (dissoc hmap
            k)))




(defn dissoc-in

  "Deep dissoc, natural counterpart of Clojure's `assoc-in`.
  
   It is recursive, meaning that if dissociating a key results in an empty map, this map itself is removed from its
   parent (provided it is of course nested).
  

   ```clojure
   (dissoc-in {:a {:b {:c {:d 42}}
                   :e 42}}
              [:a :b :c :d])
   
   {:a {:e 42}}
   ```"

  [hmap path]

  (if (seq path)
    (-dissoc-in hmap
                path)
    hmap))




(defn- -pick-right

  ;; Used by `dmerge` and `dmerge-with`.

  [_v-l v-r]

  v-r)




(defn dmerge

  "Deep merges the given maps.
  
   Merging a key pointing to nil results in that key being dissociated. Similarly to [[dissoc-in]], empty maps are recursively
   removed.
  
   ```clojure
   (dmerge {:a {:b {:c {:d 42}}
                :e 42}}
           {:a {:b {:c {:d nil}}
                :f 42}})

   {:a {:e 42
        :f 42}}
   ```"

  [& hmaps]

  (dmerge-with* -pick-right
                hmaps))




(defn dmerge-with*

  "Like [[dmerge-with]], but maps are provided in a collection."

  ([f hmaps]

   (when (some identity
               hmaps)
     (reduce (partial dmerge-with*
                      f)
             hmaps)))


  ([f hmap-1 hmap-2]

   (reduce-kv (fn pick-v [merged k v-r]
                (if (contains? merged
                               k)
                  (let [v-l (get merged
                                 k)]
                    (assoc-strict merged
                                  k
                                  (if (and (map? v-l)
                                           (map? v-r))
                                    (not-empty (dmerge-with* f
                                                             v-l
                                                             v-r))
                                    (prune (f v-l
                                              v-r)))))
                  (assoc merged
                         k
                         (prune v-r))))
               hmap-1
               hmap-2)))




(defn dmerge-with

  "Is to [[dmerge]] what this namespace's version of [[merge-with]] is to [[merge]]."

  [f & hmaps]

  (dmerge-with* f
                hmaps))




(defn merge

  "Just like standard `merge`, but a key pointing to nil in the right map means it must be dissociated.
  
   ```clojure
   (merge {:a 42
           :b 42}
          {:b nil
           :c 42})

   {:a 42
    :c 42}
   ```"

  [& hmaps]

  (merge-with* -pick-right
               hmaps))




(defn merge-with*

  "Just like this namespace's version of [[merge-with]] but maps are provided in a collection."

  [f hmaps]

  (when (some identity
              hmaps)
    (reduce (fn ??? [hmap-l hmap-r]
              (reduce-kv (fn pick-v [merged k v-r]
                           (if (contains? merged
                                          k)
                             (assoc-strict merged
                                           k
                                           (f (get merged 
                                                   k)
                                              v-r))
                             (assoc merged
                                    k
                                    v-r)))
                         hmap-l
                         hmap-r))
            hmaps)))




(defn merge-with

  "Just like standard 'merge-with' but behaves like this namespace's version of [[merge]]: a key pointing to a nil value in a
   right map means it will be dissociated. The same applies when `f` returns nil."

  [f & hmaps]

  (merge-with* f
               hmaps))




(defn no-op

  "Does absolutely nothing, but efficiently."

  ([])
  ([_])
  ([_ _])
  ([_ _ _])
  ([_ _ _ _])
  ([_ _ _ _ _])
  ([_ _ _ _ _ _])
  ([_ _ _ _ _ _ _])
  ([_ _ _ _ _ _ _ _])
  ([_ _ _ _ _ _ _ _ & _]))




(defn prune

  "If node is a map, keys with nil values will be removed. In case of nested maps, the process is recursive.

   ```clojure
   (prune {:a 42
           :b {:c 42
               :d nil
               :e {:f {:g nil}}}})
   
   {:a 42
    :b {:c 42}}


   (prune 42)

   42
   ```"

  [node]

  (if (map? node)
    (not-empty (reduce-kv (fn deeper [node-2 k v]
                            (assoc node-2
                                   k
                                   (prune v)))
                          {}
                          node))
    node))




(defn update

  "Just like standard `update` but returning a nil value results in the involved key being dissociated.
  
   ```clojure
   (update {:a 42
            :b 42}
           :b
           (fn [x]
             nil))

   {:a 42}"

  ([hmap k f]
   (assoc-strict hmap
                 k
                 (f (get hmap
                         k))))
  ([hmap k f a]
   (assoc-strict hmap
                 k
                 (f (get hmap
                         k)
                    a)))
  ([hmap k f a b]
   (assoc-strict hmap
                 k
                 (f (get hmap
                         k)
                    a
                    b)))
  ([hmap k f a b c]
   (assoc-strict hmap
                 k
                 (f (get hmap
                         k)
                    a
                    b
                    c)))
  ([hmap k f a b c & more]
   (assoc-strict hmap
                 k
                 (apply f
                        a
                        b
                        c
                        more))))




(defn- -update-in

  ;; Cf. `update-in`

  [hmap [k & ks :as path] f args]

  (if (contains? hmap
                 k)
    (assoc-strict hmap
                  k
                  (if (seq ks)
                    (not-empty (-update-in (get hmap
                                                k)
                                           ks
                                           f
                                           args))
                    (apply f 
                           (get hmap
                                k)
                           args)))
    (assoc-in hmap
              path
              (apply f
                     nil
                     args))))




(defn update-in

  "Just like standard `update-in` but like this namespace's version of [[update]], returning nil
   results in the involved key being dissociated.
  
   Similarly to [[dissoc-in]], empty maps are then recursively removed as well.
  
   When an empty path is provided, nothing happens.
  
   ```clojure
   (update-in {:a {:b {:c {:d 24}}
                   :e 42}}
              [:a :b :c :d]
              (fn [x]
                (when (= x
                         42)
                  x)))

   {:a {:e 42}}
   ```"

  [hmap path f & args]

  (if (seq path)
    (-update-in hmap
                path
                f
                args)
    (hmap)))
