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
    Object.new,
    "bar",
    999,
    LinkedList.new,
    HashMap.new,
    TreeSet.new,
    RuntimeException.new,
    IllegalArgumentException.new,
    IllegalStateException.new,
    Object.new,
    Exception.new
  ]
  result = nil
  for i in 0..1000000 do
    for o in data do
      result = o.to_s
    end
  end
  return result
end

run

