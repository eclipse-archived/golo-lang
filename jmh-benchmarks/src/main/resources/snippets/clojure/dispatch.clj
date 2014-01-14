(ns dispatch)

(defn dispatch [data]
  (doall (for [element data] (.toString element))))