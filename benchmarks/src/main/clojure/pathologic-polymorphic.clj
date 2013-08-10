(ns megamorphic)

(defn run []
  (let [data (list "foo" 666 (Object.) "bar" 999
               (java.util.LinkedList.) (java.util.HashMap.) (java.util.TreeSet.)
               (RuntimeException.) (IllegalArgumentException.) (Object.) (Exception.))]
    (dotimes [n 200000]
      (doseq [x data] (.toString x)))))