module golotest.execution.DynamicEvaluation

import gololang.EvaluationEnvironment

function maxer = {
  let env = EvaluationEnvironment(): imports("java.lang.Math")
  let max = env: func("""
     return max(a, b)
  """, "a", "b")
  return max(10, 4)
}
