(ns bench)

(defn mod500 [n] (mod n 500))

(defn lazy []
  (reduce + 0
    (filter even?
      (map inc
        (map mod500
          (take 2000000 (iterate inc 0)) )))))

(defn run [data]
  (reduce + 0
    (filter even?
      (map inc data) )))
