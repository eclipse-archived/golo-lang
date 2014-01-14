module FilterMapReduce

function run = |data| -> data:
  filter(|n| -> (n % 2_L) == 0_L):
  map(|n| -> n * 2_L):
  reduce(0_L, |acc, next| -> acc + next)
