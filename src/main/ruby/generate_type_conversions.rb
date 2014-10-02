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

# Quick and dirty script to generate the number type conversion methods

TYPES = [ :Character, :Integer, :Long, :Double, :Float ]
PRIMS = {
  :Character => :char,
  :Integer => :int,
  :Long => :long,
  :Double => :double,
  :Float => :float
}

TYPES.each do |type|
  primitive = PRIMS[type]
  puts "/**"
  puts " * Gives the #{type} value of some number or String object."
  puts " * "
  puts " * @param obj a boxed number or String value."
  puts " * @return the #{type} value."
  puts " * @throws IllegalArgumentException if {@code obj} is not a number or a String."
  puts " */"
  puts "public static Object #{primitive}Value(Object obj) throws IllegalArgumentException {"
  puts "    if (obj instanceof #{type}) {"
  puts "      return obj;"
  puts "    }"
  TYPES.reject { |t| t == type }.each do |other|
    puts "  if (obj instanceof #{other}) {"
    puts "    #{PRIMS[other]} value = (#{other}) obj;"
    puts "    return (#{primitive}) value;"
    puts "  }"
  end
  if type != :Character
    puts "  if (obj instanceof String) {"
    puts "    return #{type}.valueOf((String) obj);"
    puts "  }"
  else
    puts "  if (obj instanceof String) {"
    puts "    return ((String) obj).charAt(0);"
    puts "  }"
  end
  puts "  throw new IllegalArgumentException(\"Expected a number or a string, but got: \" + obj);"
  puts "}"
  puts
end
