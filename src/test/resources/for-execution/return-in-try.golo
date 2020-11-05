module golotest.execution.ReturnInTry

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

function fails = |lst| {
  lst: add("fails")
  raise("error")
}

function ret = |lst, msg| {
  lst: add("ret:" + msg)
  return msg
}

function test_no_return = {
  let helper = |lst| {
    try {
      lst: add("add from try")
    } catch (e) {
      lst: add(e: message())
    } finally {
      lst: add("add from finally")
    }
  }
  let lst = list[]
  helper(lst)
  assertThat(lst, contains("add from try", "add from finally"))
}

function test_no_return_fails = {
  let helper = |lst| {
    try {
      lst: add("add from try")
      fails(lst)
    } catch (e) {
      lst: add(e: message())
    } finally {
      lst: add("add from finally")
    }
  }
  let lst = list[]
  helper(lst)
  assertThat(lst, contains("add from try", "fails", "error", "add from finally"))
}

function test_in_try_no_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
      return ret(lst, "return from try")
    } catch (e) {
      lst: add(e: message())
    } finally {
      lst: add("add from finally")
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "ret:return from try", "add from finally", "return from try"))
}

function test_in_try_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
      return fails(lst)
    } catch (e) {
      lst: add(e: message())
    } finally {
      lst: add("add from finally")
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "fails", "error", "add from finally", "add after", "return after"))
}

function test_in_finally_no_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
    } catch (e) {
      lst: add(e: message())
    } finally {
      lst: add("add from finally")
      return "return from finally"
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "add from finally", "return from finally"))
}

function test_in_finally_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
      fails(lst)
    } catch (e) {
      lst: add(e: message())
    } finally {
      lst: add("add from finally")
      return "return from finally"
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "fails", "error", "add from finally", "return from finally"))
}

function test_in_try_finally_no_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
      return ret(lst, "return from try")
    } catch (e) {
      lst: add(e: message())
    } finally {
      lst: add("add from finally")
      return "return from finally"
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "ret:return from try", "add from finally", "return from finally"))
}

function test_in_try_finally_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
      return fails(lst)
    } catch (e) {
      lst: add(e: message())
    } finally {
      lst: add("add from finally")
      return "return from finally"
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "fails", "error", "add from finally", "return from finally"))
}

function test_in_catch_no_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
    } catch (e) {
      lst: add(e: message())
      return ret(lst, "return from catch")
    } finally {
      lst: add("add from finally")
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "add from finally", "add after", "return after"))
}

function test_in_catch_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
      fails(lst)
    } catch (e) {
      lst: add(e: message())
      return ret(lst, "return from catch")
    } finally {
      lst: add("add from finally")
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "fails", "error", "ret:return from catch", "add from finally", "return from catch"))
}

function test_in_catch_finally_no_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
    } catch (e) {
      lst: add(e: message())
      return ret(lst, "return from catch")
    } finally {
      lst: add("add from finally")
      return "return from finally"
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "add from finally", "return from finally"))
}

function test_in_catch_finally_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
      fails(lst)
    } catch (e) {
      lst: add(e: message())
      return ret(lst, "return from catch")
    } finally {
      lst: add("add from finally")
      return "return from finally"
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "fails", "error", "ret:return from catch", "add from finally", "return from finally"))
}

function test_in_try_catch_no_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
      return ret(lst, "return from try")
    } catch (e) {
      lst: add(e: message())
      return ret(lst, "return from catch")
    } finally {
      lst: add("add from finally")
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "ret:return from try", "add from finally", "return from try"))
}

function test_in_try_catch_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
      return fails(lst)
    } catch (e) {
      lst: add(e: message())
      return ret(lst, "return from catch")
    } finally {
      lst: add("add from finally")
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "fails", "error", "ret:return from catch", "add from finally", "return from catch"))
}

function test_in_try_catch_finally_no_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
      return ret(lst, "return from try")
    } catch (e) {
      lst: add(e: message())
      return ret(lst, "return from catch")
    } finally {
      lst: add("add from finally")
      return "return from finally"
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "ret:return from try", "add from finally", "return from finally"))
}

function test_in_try_catch_finally_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
      return fails(lst)
    } catch (e) {
      lst: add(e: message())
      return ret(lst, "return from catch")
    } finally {
      lst: add("add from finally")
      return "return from finally"
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "fails", "error", "ret:return from catch", "add from finally", "return from finally"))
}

function test_in_nested_finally_no_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
    } finally {
      lst: add("add from finally")
      try {
        lst: add("nested try")
      } finally {
        lst: add("nested finally")
        return "return from nested finally"
      }
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "add from finally", "nested try", "nested finally", "return from nested finally"))
}

function test_in_nested_finally_raise = {
  let helper = |lst| {
    try {
      lst: add("add from try")
      raise("et")
    } finally {
      lst: add("add from finally")
      try {
        lst: add("nested try")
        raise("eft")
      } finally {
        lst: add("nested finally")
        return "return from nested finally"
      }
    }
    lst: add("add after")
    return "return after"
  }
  let lst = list[]
  lst: add(helper(lst))
  assertThat(lst, contains("add from try", "add from finally", "nested try", "nested finally", "return from nested finally"))
}
