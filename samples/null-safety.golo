module DealingWithNull

import java.util

function main = |args| {

  # Data model, will be better the day we have literals for common collections
  let contacts = HashMap():
    add("mrbean",
      HashMap(): add("email", "bean@gmail.com"):
                 add("url", "http://mrbean.com")):
    add("larry",
      HashMap(): add("email", "larry@iamricherthanyou.com"))

  # MrBean and Larry
  let mrbean = contacts: get("mrbean")
  let larry = contacts: get("larry")
  
  # Illustrates orIfNull
  println(mrbean: get("url") orIfNull "n/a")
  println(larry: get("url") orIfNull "n/a")

  # Querying a non-existent data model because there is no 'address' entry
  println(mrbean: get("address")?: street()?: number() orIfNull "n/a")
}
