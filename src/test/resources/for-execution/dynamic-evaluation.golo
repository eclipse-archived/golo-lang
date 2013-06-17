module golotest.execution.DynamicEvaluation

import gololang.EvaluationEnvironment

function maxer = {
  let env = EvaluationEnvironment(): imports("java.lang.Math")
  let max = env: asFunction("""
     return max(a, b)
  """, "a", "b")
  return max(10, 4)
}

function run_plop = {
  let env = EvaluationEnvironment()
  return env: run("""
    let a = 1
    let b = 2
    let f = |x, y| -> x + y
    return f(a, b)
  """)
}
