def run
  runner =  lambda do |value|
    return "[" + value.to_s + "]"
  end
  result = nil
  for i in 0..2000000 do
    result = runner.call(i)
  end
  return result
end

run
