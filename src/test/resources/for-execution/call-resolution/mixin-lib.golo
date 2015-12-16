module golotest.augmentations.MyLib

function sayPlop = |o| -> match {
  when o: isPlopable() then "say:" + o: plop()
  otherwise "no plop:" + o: toString()
}
