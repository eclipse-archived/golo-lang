# Copyright (c) 2012-2020 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# SPDX-License-Identifier: EPL-2.0

# Quick and dirty script to generate the arithmetic operation methods

## On primitive types (box/unbox)
OPS_SYMB = {
  :plus => '+',
  :minus => '-',
  :divide => '/',
  :times => '*',
  :modulo => '%',
  :equals => '==',
  :notequals => '!=',
  :less => '<',
  :lessorequals => '<=',
  :more => '>',
  :moreorequals => '>='
}

PRIM = {
  :Character => :char,
  :Integer => :int,
  :Long => :long,
  :Double => :double,
  :Float => :float
}

WEIGHT = {
  :Character => 1,
  :Integer => 2,
  :Long => 3,
  :Float => 4,
  :Double => 5
}

PRIM.each do |type, prim|
  OPS_SYMB.each do |op, symb|
    puts "  public static Object #{op}(#{type} a, #{type} b) {"
    puts "    return ((#{prim}) a) #{symb} ((#{prim}) b);"
    puts "  }"
    puts
  end
end

combinations = PRIM.keys().combination(2).to_a
combinations = combinations + combinations.map { |pair| [pair[1], pair[0]] }
combinations.each do |pair|
  left = pair[0]
  right = pair[1]
  OPS_SYMB.each do |op, symb|
    puts "  public static Object #{op}(#{left} a, #{right} b) {"
    if WEIGHT[left] < WEIGHT[right]
        type = PRIM[right]
    else
        type = PRIM[left]
    end
    puts "    return ((#{type}) a) #{symb} ((#{type}) b);"
    puts "  }"
    puts
  end
end

# ..........................................................................
# On BigDecimal/BigInteger

INT_NUMBERS = [ :Integer, :Long, :BigInteger ]
REAL_NUMBERS = [ :Float, :Double ]
COMPARISONS = [ :equals, :notequals, :less, :lessorequals, :more, :moreorequals ]
OPS_METH = {
  :plus => :add,
  :minus => :subtract,
  :times => :multiply,
  :divide => :divide,
  :modulo => :remainder
}

toBigDecimal = -> (arg, type) { case type
  when :BigDecimal then arg
  else "new BigDecimal(#{arg})"
  end }


toBigInteger = -> (arg, type) { case type
  when :BigInteger then arg
  when :BigDecimal then "#{arg}.toBigInteger()"
  else "BigInteger.valueOf(#{arg}.longValue())"
end }

def generateOperators(numbers, the_type, the_conversion)
  numbers.each do |type|
    COMPARISONS.each do |op|
      puts "  public static Object #{op}(#{the_type} a, #{type} b) {"
      puts "    return (#{the_conversion.("a", the_type)}).compareTo(#{the_conversion.("b", type)}) #{OPS_SYMB[op]} 0;"
      puts "  }"
      puts
      if type != the_type
        puts "  public static Object #{op}(#{type} a, #{the_type} b) {"
        puts "    return (#{the_conversion.("a", type)}).compareTo(#{the_conversion.("b", the_type)}) #{OPS_SYMB[op]} 0;"
        puts "  }"
        puts
      end
    end
    OPS_METH.each do |op, meth|
      puts "  public static Object #{op}(#{the_type} a, #{type} b) {"
      puts "    return (#{the_conversion.("a", the_type)}).#{meth}(#{the_conversion.("b", type)});"
      puts "  }"
      puts
      if type != the_type
        puts "  public static Object #{op}(#{type} a, #{the_type} b) {"
        puts "    return (#{the_conversion.("a", type)}).#{meth}(#{the_conversion.("b", the_type)});"
        puts "  }"
        puts
      end
    end
  end
end


generateOperators(INT_NUMBERS, :BigDecimal, toBigDecimal)
generateOperators(REAL_NUMBERS + [ :BigDecimal ], :BigDecimal, toBigDecimal)
generateOperators(INT_NUMBERS, :BigInteger, toBigInteger)
generateOperators(REAL_NUMBERS, :BigInteger, toBigDecimal)

