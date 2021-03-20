(ns helins.void.test

  {:author "Adam Helinski"}

  (:require [clojure.core :as clj]
            [clojure.test :as t]
            [helins.void  :as void])
  (:refer-clojure :exclude [assoc
                            assoc-in
                            merge
                            merge-with
                            update
                            update-in]))




;;;;;;;;;;


(t/deftest alt

  (t/is (= false
           (void/alt nil
                     false
                     true))
        "Picks the first non-nil value"))




(t/deftest assoc

  (t/is (= {:a 42
            :d 42}
           (void/assoc {:a 24}
                       :a 42
                       :b nil
                       :c nil
                       :d 42))))




(t/deftest assoc-in

  (t/is (= {}
           (void/assoc-in {}
                          [:a :b :c]
                          nil))))




(t/deftest assoc-strict

  (t/is (= {:a 42
            :d 42}
           (void/assoc-strict {:a 24
                               :b 24
                               :c 24}
                              :a 42
                              :b nil
                              :c nil
                              :d 42))))



(t/deftest assoc-strict-in

  (t/is (= {:a {:b 42}}
           (void/assoc-strict-in {}
                                 [:a :b]
                                 42))
        "Non-nil value")

  (t/is (= {:a {:b 42}}
           (void/assoc-strict-in {:a {:b 42}}
                                 [:A :C]
                                 nil))
        "Nil value on non-existing path")

  (t/is (= {:a {:b 42}}
           (void/assoc-strict-in {:a {:b 42}
                                  :c {:d 42}}
                                 [:c :d]
                                 nil))
        "Nil value, path is removed."))



(t/deftest call

  (t/is (nil? (void/call ((fn []))
                         42)))

  (t/is (= 2
           (void/call inc
                      1))))




(t/deftest dissoc-in


  (t/is (= {:d 42}
           (void/dissoc-in {:a {:b {:c 42}}
                            :d 42}
                           [:a :b :c]))
        "Dissoc'ing only at the path and nothing else")


  (t/is (= {:a {:e 42}}
           (void/dissoc-in {:a {:b {:c {:d 42}}
                                :e 42}}
                           [:a :b :c :d]))
        "Recursively dissoc empty map only")


  (let [tree {:a {:b {:c 42}}}]
    (t/is (= tree
             (void/dissoc-in tree
                             [:a :b :c :d]))
          "Dissoc'ing a path deeper than the tree does not do anything because there is nothing to dissoc"))


  (t/is (= {:a {:b 42}}
           (void/dissoc-in {:a {:b 42
                                :c {:d 42}}}
                           [:a :c]))
        "Dissoc'ing a branch does not do anything else")


  (t/is (= {:a {:b 42}}
           (void/dissoc-in {:a {:b 42}}
                           [:c :d]))
        "Dissoc'ing a non-existing path does not do anything")


  (t/is (= {:a 42}
           (void/dissoc-in {:a 42}
                           []))
        "Dissoc'ing an empty path does not do anything")


  (t/is (= {}
           (void/dissoc-in {}
                           [:a :b]))
        "Dissoc'ing an empty map does not do anything"))




(t/deftest dmerge

  (let [merged (void/dmerge {:a :before
                             :b {:c :before
                                 :d {:e :before}}}
                            {:a :after
                             :b {:c :after
                                 :d :after}
                             :e {:f :after}})]

    ;; Merges recursively.

    (t/are [path]
           (= :after
              (get-in merged
                      path))
      [:a]
      [:b :c]
      [:b :d]
      [:e :f]))


  (t/is (= {:a {:b :after
                :c {:k {:l {:m :after}}}}}
           (void/dmerge {:a {:b :before
                             :c {:d :before
                                 :e :before}}}
                        {:a {:b :after
                             :c {:d nil
                                 :e {:f {:g nil}}
                                 :h {:i {:j nil}}
                                 :k {:l {:m :after}}}}}))
        "Nil/empty values are recursively dissoc'ed"))




(t/deftest merge

  ;; Tests `void/merge-with*"

  (t/is (= {:a :after
            :c :added}
           (void/merge {:a :before
                        :b :before}
                       {:a :after
                        :b nil
                        :c :added}))
        "New nil values are removed, otherwise behaves like standard `merge`"))




(t/deftest prune

  (t/is (= 42
           (void/prune 42))
        "Pruning something else than a map does nothing")

  (t/is (= nil
           (void/prune {:a {:b {:c nil}}}))
        "Pruning empty leafs leaves nothing")


  (t/is (= {:a {:b 42}}
           (void/prune {:a {:b 42
                            :c {:d nil}}}))
        "Non-nil leaves are kept intact"))




(t/deftest update
  
  (t/is (= {:a 43}
           (void/update {:a 42}
                        :a
                        inc))
        "Behaves like standard `update` in the presence of an existing value")

  (t/is (= {:a 42}
           (void/update {}
                        :a
                        (constantly 42)))
        "Behaves like standard `update` in the absence an of existing value")

  (t/is (= {}
           (void/update {:a 42}
                        :a
                        (constantly nil)))
        "Dissoc when the new value is nil")

  (t/is (= {}
           (void/update {}
                        :a
                        identity))
        "Do not assoc nil in the absence of an existing value"))




(t/deftest update-in

  (let [path [:a :b :c]
        hmap (clj/assoc-in {}
                           path
                           42)]
    (t/is (= (clj/update-in hmap
                            path
                            inc)
             (void/update-in hmap
                             path
                             inc))
          "Behaves like standard `update-in` in the presence of an existing value"))

  (let [path [:a :b :c]
        f    (constantly 42)]
    (t/is (= (clj/update-in {}
                            path
                            f)
             (void/update-in {}
                             path
                             f))
          "Behaves like standard `update-in` in the absence of an existing value"))

  (t/is (= {}
           (void/update-in {:a {:b {:c 42}}}
                           [:a :b :c]
                           (constantly nil)))
        "Recursively dissoc when new value is nil")

  (t/is (= {}
           (void/update-in {}
                           [:a :b :c]
                           identity))
        "Do not assoc nil in the absence of an existing value"))
