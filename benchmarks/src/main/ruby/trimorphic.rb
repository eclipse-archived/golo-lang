require 'java'

java_import 'java.lang.RuntimeException'
java_import 'java.lang.IllegalArgumentException'
java_import 'java.lang.IllegalStateException'
java_import 'java.util.LinkedList'
java_import 'java.util.HashMap'
java_import 'java.util.TreeSet'

def run
  data = [
    "foo",
    666,
    "bar",
    999,
    "plop",
    "da",
    "plop",
    "for",
    "ever",
    1,
    2,
    3,
    4,
    5,
    6,
    Object.new,
    Object.new,
    Object.new,
    Object.new
  ]
  result = nil
  for i in 0..200000 do
    for o in data do
      result = o.to_s
    end
  end
  return result
end

run

