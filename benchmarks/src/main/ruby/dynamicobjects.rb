require 'java'

def run
  obj = Object.new
  obj.instance_variable_set("@random", java.util.Random.new)
  def obj.rand
    @random.nextInt()
  end
  obj.instance_variable_set("@acc", 0)
  def obj.acc
    @acc
  end
  def obj.acc=(value)
    @acc = value
  end
  i = 0
  while i < 5000000 do
    obj.acc = obj.acc + obj.rand()
    i = i + 1
  end
  return obj.acc
end

run

