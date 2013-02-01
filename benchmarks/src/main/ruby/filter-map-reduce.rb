@data_set.map{|x| x + 1}.select{|x| x % 2 == 0}.inject(0) {|acc, x| acc + x}

