(ns dvlopt.rktree

  "README describes what ranked trees are.

   This namespace provides recognizable functions for manipulating them."

  {:author "Adam Helinski"}

  (:require [clojure.core :as clj]
            [dvlopt.void  :as void])
  ;;
  ;; <!> Attention <!>
  ;; 
  ;; Code is confusing if one does not remember this.
  ;;
  (:refer-clojure :exclude [#?@(:cljs [-assoc
                                       -pop])
                            assoc
                            dissoc
                            get
                            pop
                            update]))




;;;;;;;;;; Gathering all declarations


(declare update)




;;;;;;;;;; Creating new trees


(defn tree

  "Simply provides a sorted map, reminding that a ranked tree always start with at least one sorted map.
  
   Should be used just in case the implementation changes and uses something else than Clojure's sorted maps."

  []

  (sorted-map))




;;;;;;;;;; Operations with ranks


(defn r+

  "Sums the given rank collections which might be of different length.
  
   ```clojure
   (r+ [0  5]
       [10 10 10])

   [10 15 10]
   ```"

  ;; TODO. Optimize for endings in 0's ?

  [ranks-1 ranks-2]

  (let [n-1      (count ranks-1)
        n-2      (count ranks-2)
        [base
         [front
          rear]] (if (<= n-1
                         n-2)
                   [ranks-1
                    (split-at n-1
                              ranks-2)]
                   [ranks-2
                    (split-at n-2
                              ranks-1)])]
    (into (mapv +
                base
                front)
          rear)))




;;;;;;;;;; Manipulating trees


(defn- -enforce-ranks

  ;; At least one rank must be specified, there is no concept of ranked trees without
  ;; sorted maps surrounding the root.

  [ranks]

  (if (seq ranks)
    ranks
    (throw (ex-info "Ranks cannot be empty"
                    {::ranks ranks}))))




(defn- -assoc-in

  ;; Returns `v` when there is no path.

  ([path v]

   (-assoc-in {}
              path
              v))


  ([hmap path v]

   (if (empty? path)
     v
     (assoc-in hmap
               path
               v))))




(defn- -assoc-leaf

  ;; Creates required sorted maps following ranks then associates the leaf.

  [[r & rs] path v]

  (sorted-map r (if rs
                  (-assoc-leaf rs
                               path
                               v)
                  (-assoc-in path
                             v))))




(defn- -bubbling-assoc-leaf

  ;; Like [[-assoc-leaf]], but incorporates a "bubbling" node which needs to be re-prioritized.

  [[r & rs] path v bubbling-node]

  (if (zero? r)
    (if rs
      (sorted-map 0 (-bubbling-assoc-leaf rs
                                          path
                                          v
                                          bubbling-node))
      (-assoc-in bubbling-node
                 path
                 v))
    (if rs
      (sorted-map 0 bubbling-node
                  r (-assoc-leaf rs
                                 path
                                 v))
      (sorted-map 0 bubbling-node
                  r (-assoc-in path
                               v)))))



(defn- -assoc

  ;; Cf. [[assoc]]

  [tree [r & rs] path v]

  (clj/update tree
              r
              (fn at-rank [node]
                (cond
                  (nil? node)    (if rs
                                   (-assoc-leaf rs
                                                path
                                                v)
                                   (-assoc-in path
                                              v))
                  (sorted? node) (if rs
                                   (-assoc node
                                           rs
                                           path
                                           v)
                                   (-assoc node
                                           [0]
                                           path
                                           v))
                  :else          (if rs
                                   (if (some? node)
                                     (-bubbling-assoc-leaf rs
                                                           path
                                                           v
                                                           node)
                                     (-assoc-leaf rs
                                                  path
                                                  v))
                                   (-assoc-in node
                                              path
                                              v))))))




(defn assoc

  "Associates `v` at `path` which is then prioritized using `ranks`.

   When `path` is not provided, associates directly after the ranks.
  
   Cf. Namespace description for an example."

  ([tree ranks v]

   (assoc tree
          ranks
          nil
          v))


  ([tree ranks path v]

   (-assoc tree
           (-enforce-ranks ranks)
           path
           v)))




(defn dissoc

  "Dissociates a value located at `path` and prioritized by `ranks`.
  
   When `path` is not provided, dissociates everything for the given `ranks`."

  ([tree ranks]

   (dissoc tree
           ranks
           nil))


  ([tree ranks path]

   (update tree
           ranks
           nil
           (fn dissoc-in [node]
             (when (seq path)
               (not-empty (void/dissoc-in node
                                          path)))))))




(defn- -get

  ;; Cf. [[get]]

  [tree [r & rs] path not-found]

  (let [node (clj/get tree
                      r)]
    (if (sorted? node)
      (if rs
        (recur node
               rs
               path
               not-found)
        (recur node
               [0]
               path
               not-found))
      (if rs
        not-found
        (get-in node
                path
                not-found)))))




(defn get

  "Find a value located at `path` that has been prioritized with `ranks`.
  
   When path is not provided, returns everything at the given `ranks`."

  ([tree ranks]

   (get tree
        ranks
        nil))


  ([tree ranks path]

   (get tree
        ranks
        path
        nil))


  ([tree ranks path not-found]

   (-get tree
         (-enforce-ranks ranks)
         path
         not-found)))




(defn- -pop

  ;; Cf. [[pop]]

  [node ranks]

  (if (and (map? node)
           (sorted? node))
    (if-some [[k
               node-next] (first node)]
      (clj/update (-pop node-next
                        (conj ranks
                              k))
                  0
                  (fn rebuild-tree [subtree]
                    (if subtree
                      (clj/assoc node
                                 k
                                 subtree)
                      (not-empty (clj/dissoc node
                                             k)))))
      [nil
       ranks
       nil])
    [nil
     ranks
     node]))




(defn pop

  "Respecting the ordering of ranks, returns a vector such as:
  
   | i | meaning |
   |---|---|
   | 0 | tree after popping |
   | 1 | ranks that led to node |
   | 2 | popped unsorted node at ranks |

   There might be no node to pop, and after popping a empty tree is returned as nil."

  [tree]

  (-pop tree
        []))




(defn- -walk-unsorted

  ;; Cf. [[-pop-walk]]

  [acc ranks path node f]

  (if (map? node)
    (reduce-kv (fn deeper [acc-2 k node-next]
                 (-walk-unsorted acc-2
                                 ranks
                                 (conj path
                                       k)
                                 node-next
                                 f))
               acc
               node)
    (f acc
       ranks
       path
       node)))




(defn pop-walk

  "Pops the given ranked `tree` and then walks the unsorted node in a depth-first manner by applying:

   ```clojure
   (f acc
      ranks
      path
      leaf)
   ```

   After popping but before walking the unsorted node, the popped tree is transformed into `acc`
   by calling `popped-tree->acc` (defaulting to Clojure's `identity`).

   See also [[pop]]."

  ([tree f]

   (pop-walk tree
             identity
             f))


  ([tree popped-tree->acc f]

   (if (sorted? tree)
     (let [[tree-2
            ranks
            node]  (pop tree)]
      (if (nil? node)
        tree-2
        (-walk-unsorted (popped-tree->acc tree-2)
                        ranks
                        []
                        node
                        f)))
     (-walk-unsorted (popped-tree->acc nil)
                     []
                     []
                     tree
                     f))))




(defn- -update

  ;; Cf. [[update]]

  [tree [r & rs :as ranks] path f]

  (void/update tree
               r
               (fn at-rank [node]
                 (if node
                   (if rs
                     (if (sorted? node)
                       (not-empty (-update node
                                           rs
                                           path
                                           f))
                       (if-some [leaf (f nil)]
                         (-bubbling-assoc-leaf rs
                                               path
                                               leaf
                                               node)
                         node))
                     (if (sorted? node)
                       (not-empty (-update node
                                             [0]
                                             path
                                             f))
                       (if (seq path)
                         (not-empty (void/update-in node
                                                    path
                                                    f))
                         (f node))))
                   (when-some [v (f nil)]
                     (if rs
                       (-assoc-leaf rs
                                    path
                                    v)
                       (-assoc-in path
                                  v)))))))




(defn update

  "Update the value located at `path` and prioritized by `ranks`
  
   When `path` is not provided, updates whatever there is at the given `ranks`.
  
   If the result of applying `f` is nil, the value is removed."

  ([tree ranks f]

   (update tree
           ranks
           nil
           f))


  ([tree ranks path f]

   (-update tree
            (-enforce-ranks ranks)
            path
            f)))
