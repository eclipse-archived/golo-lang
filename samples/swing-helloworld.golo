module test

import javax.swing
import javax.swing.WindowConstants

function main = |args| {

  let frame = JFrame("Hello world")
  frame: setDefaultCloseOperation(EXIT_ON_CLOSE())

  let label = JLabel("Hello world")
  label: setFont(label: getFont(): deriveFont(128.0_F))

  frame: getContentPane(): add(label)
  frame: pack()
  frame: setVisible(true)
}

