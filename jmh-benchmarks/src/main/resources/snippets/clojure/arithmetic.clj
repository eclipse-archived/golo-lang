(ns arithmetic)

(defn gcd [a b]
  (if (= a b)
    a
    (if (> a b)
      (recur (- a b) b)
      (recur a (- b a)) )))

(defn ^long fast-gcd [^long a ^long b]
  (if (= a b)
    a
    (if (> a b)
      (recur (- a b) b)
      (recur a (- b a)) )))
