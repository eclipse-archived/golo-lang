(ns dynobjs)

(definterface ISampleObject
  (meth [])
  (value []))

(deftype SampleObject [^{:volatile-mutable true} acc ^{:volatile-mutable true} fun] ISampleObject
  (meth [this]
    (set! acc (+ (.value this) (fun))))
  (value [this] acc))

(defn run[]
  (let [o (SampleObject. 0 (partial rand-int 32768))]
    (dotimes [n 5000000] (.meth o))))