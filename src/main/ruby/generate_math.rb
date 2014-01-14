# Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Quick and dirty script to generate the arithmetic operation methods

TYPES = [ :Character, :Integer, :Long, :Double, :Float ]

OPS = [ :plus, :minus, :divide, :times, :modulo ]

OPS_SYMB = {
  :plus => '+',
  :minus => '-',
  :times => '*',
  :divide => '/',
  :modulo => '%'
}

PRIM = {
  :Character => :char,
  :Integer => :int,
  :Long => :long,
  :Float => :float,
  :Double => :double
}

WEIGHT = {
  :Character => 1,
  :Integer => 2,
  :Long => 3,
  :Float => 4,
  :Double => 5
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

