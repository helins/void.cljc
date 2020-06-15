(ns user

  "For daydreaming in the REPL." 

  (:require [clojure.repl]
            [clojure.test               :as t]
            [cognitect.transit          :as transit]
            [criterium.core             :as C]
            [dvlopt.rktree              :as rktree]
            [dvlopt.rktree.transit      :as rktree.transit]
            [dvlopt.rktree-test         :as rktree-test]
            [dvlopt.rktree.transit-test :as rktree.transit-test]
            [dvlopt.void                :as void]))




;;;;;;;;;;


;(require '[nrepl.server])  (defonce server (nrepl.server/start-server :port 4000))



(comment

  ;; Examples from README


  (def my-tree
       (sorted-map 0 (sorted-map 1 {:a {:b 'leaf-1}})
                   5 {:a {:d {:e 'leaf-2}}}))


  (= 'leaf-1
     (rktree/get my-tree
                 [0 1]
                 [:a :b]))


  (= (rktree/pop my-tree)

     [(sorted-map 5 {:a {:d {:e 'leaf-2}}})
      [0 1]
      {:a {:b 'leaf-1}}])


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
  )
