def gcd(x, y, repeat)
  res = 0
  for i in 0..repeat do
    a = x
    b = y
    while a != b
      if a > b
        a = a - b
      else
        b = b - a
      end
    end
    res = a
  end
  return res
end
