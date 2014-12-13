module InitializedReference

function main = |args| {
  let func = |e| {
    return e + 1
  }

 try {
   return func(41)
 } catch(e) {
    # may never happened
 }
}
