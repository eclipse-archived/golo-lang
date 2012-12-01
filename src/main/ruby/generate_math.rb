# Quick and dirty script to generate the arithmetic operation methods

TYPES = [ :Integer, :Long, :Double, :Float ]

OPS = [ :plus, :minus, :divide, :times, :modulo ]

OPS_SYMB = {
  :plus => '+',
  :minus => '-',
  :times => '*',
  :divide => '/',
  :modulo => '%'
}

PRIM = {
  :Integer => :int,
  :Long => :long,
  :Float => :float,
  :Double => :double
}

WEIGHT = {
  :Integer => 1,
  :Long => 2,
  :Float => 3,
  :Double => 4
}

TYPES.each do |type|
  OPS.each do |op|
    puts "public static Object #{op}(#{type} a, #{type} b) {"
    puts "  return a #{OPS_SYMB[op]} b;"
    puts "}"
    puts
  end
end

combinations = TYPES.combination(2).to_a
combinations = combinations + combinations.map { |pair| [pair[1], pair[0]] }
combinations.each do |pair|
  left = pair[0]
  right = pair[1]
  OPS.each do |op|
    puts "public static Object #{op}(#{left} a, #{right} b) {"
    if WEIGHT[left] < WEIGHT[right]
      puts "  return ((#{PRIM[right]}) a) #{OPS_SYMB[op]} b;"
    else
      puts "  return a #{OPS_SYMB[op]} ((#{PRIM[left]}) b);"
    end
    puts "}"
    puts
  end
end

