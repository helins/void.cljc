(ns dvlopt.void

  "Macros and functions for handling nil under various circumstances." 

  {:author "Adam Helinski"})




;;;;;;;;;;


(defmacro alt

  "Selects the first non-nil value, akin to the standard macro `or`.
  
   Ex. (dvlopt.void/alt nil
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




(defmacro call

  "Calls `f` with the given arguments only if `f` is not nil."

  [f & args]

  (when (some? f)
    `(when-some [f'# ~f]
       (f'# ~@args))))




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




(defn last-existing

  "Selects the rightmost non-nil argument.

   It is a function useful for things such as `merge-with`."

  ([])
  ([a]
   a)
  ([a b]
   (alt b a))
  ([a b c]
   (alt c b a))
  ([a b c d]
   (alt d c b a))
  ([a b c d e]
   (alt e d c b a))
  ([a b c d e f]
   (alt f e d c b a))
  ([a b c d e f g]
   (alt g f e d c b a))
  ([a b c d e f g h]
   (alt h g f e d c b a))
  ([a b c d e f g h & xs]
   (alt (apply last-existing
               xs)
        h
        g
        f
        e
        d
        c
        b
        a)))




(defn assoc-some

  "Behaves like standard `assoc` but only when the `v` is not nil."

  ([hmap k v]

   (if (nil? v)
     hmap
     (assoc hmap
            k
            v)))


  ([hmap k v & kvs]

   (let [hmap' (assoc-some hmap
                           k
                           v)]
     (if (seq kvs)
       (recur hmap'
              (first kvs)
              (second kvs)
              (nnext kvs))
       hmap'))))





(defn obtain

  "Looks for a non-nil value using key `k` on the given maps, in the given order."

  ([k m1]
   (get m1 k))
  ([k m1 m2]
   (alt (get m1 k)
        (get m2 k)))
  ([k m1 m2 m3]
   (alt (get m1 k)
        (get m2 k)
        (get m3 k)))
  ([k m1 m2 m3 m4]
   (alt (get m1 k)
        (get m2 k)
        (get m3 k)
        (get m4 k)))
  ([k m1 m2 m3 m4 m5]
   (alt (get m1 k)
        (get m2 k)
        (get m3 k)
        (get m4 k)
        (get m5 k)))
  ([k m1 m2 m3 m4 m5 m6]
   (alt (get m1 k)
        (get m2 k)
        (get m3 k)
        (get m4 k)
        (get m5 k)
        (get m6 k)))
  ([k m1 m2 m3 m4 m5 m6 m7]
   (alt (get m1 k)
        (get m2 k)
        (get m3 k)
        (get m4 k)
        (get m5 k)
        (get m6 k)
        (get m7 k)))
  ([k m1 m2 m3 m4 m5 m6 m7 m8]
   (alt (get m1 k)
        (get m2 k)
        (get m3 k)
        (get m4 k)
        (get m5 k)
        (get m6 k)
        (get m7 k)
        (get m8 k)))
  ([k m1 m2 m3 m4 m5 m6 m7 m8 & ms]
   (alt (get m1 k)
        (get m2 k)
        (get m3 k)
        (get m4 k)
        (get m5 k)
        (get m6 k)
        (get m7 k)
        (get m8 k)
        (apply obtain
               k
               ms))))




(defmacro ^:private -select

  "Helper for `select`."

  [ks & hmaps]

  `(reduce (fn ~'add-v [hmap# k#]
             (assoc-some hmap#
                         k#
                         (obtain k#
                                 ~@hmaps)))
           {}
           ~ks))




(defn select 

  "Looks for a non-nil value for eack key in `ks`, in the given maps, in the given order."

  ([ks m1]
   (-select ks m1))
  ([ks m1 m2]
   (-select ks m1 m2))
  ([ks m1 m2 m3]
   (-select ks m1 m2 m3))
  ([ks m1 m2 m3 m4]
   (-select ks m1 m2 m3 m4))
  ([ks m1 m2 m3 m4 m5]
   (-select ks m1 m2 m3 m4 m5))
  ([ks m1 m2 m3 m4 m5 m6]
   (-select ks m1 m2 m3 m4 m5 m6))
  ([ks m1 m2 m3 m4 m5 m6 m7]
   (-select ks m1 m2 m3 m4 m5 m6 m7))
  ([ks m1 m2 m3 m4 m5 m6 m7 m8]
   (-select ks m1 m2 m3 m4 m5 m6 m7 m8))
  ([ks m1 m2 m3 m4 m5 m6 m7 m8 & ms]
   (reduce (fn add-v [hmap k]
             (assoc-some hmap
                         k
                         (apply obtain
                                k
                                m1
                                m2
                                m3
                                m4
                                m5
                                m6
                                m7
                                m8
                                ms)))
           {}
           ks)))
