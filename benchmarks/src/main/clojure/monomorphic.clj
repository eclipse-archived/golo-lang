(ns monomorphic)

(defn run []
  (let [value (Object.)]
    (dotimes [n 5000000] (.toString value))))