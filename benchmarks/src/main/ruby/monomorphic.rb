def run
  result = nil
  i = 0
  while i < 5000000 do
    result = i.to_s
    i = i + 1
  end
  return result
end

run

