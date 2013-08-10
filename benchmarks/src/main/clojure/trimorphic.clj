(ns trimorphic)

(defn run []
  (let [data (list "foo" 666 "bar" 999 "plop" "da" "plop" "for" "ever"
               1 2 3 4 5 6 (Object.) (Object.) (Object.) (Object.))]
    (dotimes [n 200000]
      (doseq [x data] (.toString x)))))
