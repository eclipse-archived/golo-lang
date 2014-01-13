def run(data)
  data.select{|n| n % 2 == 0}.map{|n| n * 2}.inject(0) {|acc, n| acc + n}
end