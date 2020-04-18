(ns dvlopt.void

  "Macros and functions for handling nil under various circumstances." 

  {:author "Adam Helinski"}

  (:require [clojure.core :as clj]
            [dvlopt.void  :as void])
  (:refer-clojure :exclude [assoc
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

  "Selects the first non-nil value, akin to the standard macro `or`.
  
   Ex. (alt nil
            false
            42)
       => false"

  ([]

   nil)


  ([x]

   x)


  ([x & xs]

   `(if-some [x'# ~x]
      x'#
      (alt ~@xs))))




(defn- -assoc

  ;;

  ([on-nil hmap k v]

   (if (nil? v)
     (on-nil hmap
             k)
     (clj/assoc hmap
                k
                v)))


  ([on-nil hmap k v kvs]

   (let [hmap-2 (void/-assoc on-nil
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

  ;;

  [hmap _k]

  hmap)



(defn assoc

  "Behaves like standard `assoc` but associates `v` only it is not nil."

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

  ""

  [hmap path v]

  (if (and (seq path)
           (some? v))
    (clj/assoc-in hmap
                  path
                  v)
    hmap))




(defn assoc-strict

  ""

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

  "Calls `f` with the given arguments only if `f` is not nil."

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
  
   <!> Keys with nil values or empty maps are removed.
  
  
   Eg. (dissoc-in {:a {:b 42}
                   :c :ok}
                  [:a :b])
  
       => {:c :ok}"

  ;; TODO. When empty path, return nil?

  [hmap path]

  (if (seq path)
    (-dissoc-in hmap
                path)
    hmap))




(defn- -pick-right

  ;;

  [_v-l v-r]

  v-r)




(defn dmerge

  ""

  [& hmaps]

  (dmerge-with* -pick-right
                hmaps))




(defn dmerge-with*

  ""

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
                  (void/assoc merged
                              k
                              (prune v-r))))
               hmap-1
               hmap-2)))




(defn dmerge-with

  ""

  [f & hmaps]

  (dmerge-with* f
                hmaps))




(defn merge

  ""

  [& hmaps]

  (merge-with* -pick-right
               hmaps))




(defn merge-with*

  ;;

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
                             (void/assoc merged
                                         k
                                         v-r)))
                         hmap-l
                         hmap-r))
            hmaps)))




(defn merge-with

  ""

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

  ""

  [node]

  (if (map? node)
    (not-empty (reduce-kv (fn deeper [node-2 k v]
                            (void/assoc node-2
                                        k
                                        (prune v)))
                          {}
                          node))
    node))




(defn update

  ""

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

  ;;

  [hmap [k & ks :as path] f]

  (if (contains? hmap
                 k)
    (assoc-strict hmap
                  k
                  (if (seq ks)
                    (not-empty (-update-in (get hmap
                                                k)
                                           ks
                                           f))
                    (f (get hmap
                            k))))
    (void/assoc-in hmap
                   path
                   (f nil))))




(defn update-in

  ""

  [hmap path f]

  (if (seq path)
    (-update-in hmap
                path
                f)
    (f hmap)))








