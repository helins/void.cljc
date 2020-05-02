(ns dvlopt.rktree.transit-test

  {:author "Adam Helinski"}

  (:require [clojure.test          :as t]
            [cognitect.transit     :as transit]
            [dvlopt.rktree.transit :as rktree.transit])
  #?(:clj (:import (java.io ByteArrayInputStream
                            ByteArrayOutputStream))))




;;;;;;;;;; Ser/de


(defn serialize

  "Serializes using Transit."

  [x]

  (let [options {:handlers rktree.transit/write-handler}]
    #?(:clj  (let [out (ByteArrayOutputStream. 512)]
               (transit/write (transit/writer out
                                              :json
                                              options)
                              x)
               out)
       :cljs (transit/write (transit/writer :json
                                            options)
                            x))))




(defn deserialize

  "Deserializes using Transit."

  [x]

  (transit/read
    (transit/reader #?(:clj (ByteArrayInputStream. (.toByteArray x)))
                    :json
                    {:handlers rktree.transit/read-handler})
    #?(:cljs x)))




(defn serde-twice

  "Serializes and deserializes twice using Transit."

  [sorted]

  (let [sorted-2 (-> sorted
                     serialize
                     deserialize
                     serialize
                     deserialize)]
    (if (sorted? sorted-2)
      sorted-2
      (throw (ex-info "Sorted map is not sorted at deserialization"
                      {::x sorted-2})))))




;;;;;;;;;;


(def my-tree
     (sorted-map 0 (sorted-map 1 {:a {:b 'leaf-1}})
                 5 {:a {:d {:e 'leaf-2}}}))


(t/deftest serde

  (let [my-tree-2 (serde-twice my-tree)]

    (t/is (= my-tree
             my-tree-2)
          "Serde of a ranked tree")

    (t/is (and (sorted? my-tree-2)
               (sorted? (get my-tree-2
                             0)))
          "Checking ordering is maintained")))
