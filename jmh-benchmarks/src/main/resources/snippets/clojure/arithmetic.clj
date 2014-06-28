(ns arithmetic)

(defn -gcd [a b]
  (if (= a b)
    a
    (if (> a b)
      (recur (- a b) b)
      (recur a (- b a)) )))

(defn ^long -fast-gcd [^long a ^long b]
  (if (= a b)
    a
    (if (> a b)
      (recur (- a b) b)
      (recur a (- b a)) )))

(defn gcd [a b repeat-count]
  (let [res (-gcd a b)]
    (if (zero? repeat-count)
      res
      (recur a b (dec repeat-count)) )))

(defn ^long fast-gcd [^long a ^long b ^long repeat-count]
  (let [res (-fast-gcd a b)]
    (if (zero? repeat-count)
      res
      (recur a b (dec repeat-count)) )))

