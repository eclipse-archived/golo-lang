module FilterMapReduce

function run = |list| -> list:
  map(|x| -> x + 2):
  filter(|x| -> (x % 2) is 0):
  reduce(0, |acc, x| -> acc + x)
