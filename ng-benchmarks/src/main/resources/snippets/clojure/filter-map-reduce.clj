(ns filter-map-reduce)

(defn run [data]
  (reduce + 0
    (map #(* 2 %)
      (filter even? data) )))
