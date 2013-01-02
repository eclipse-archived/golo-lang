module test

import java.awt.event
import javax.swing
import javax.swing.WindowConstants

function main = |args| {

  let frame = JFrame("Action listeners")
  frame: setDefaultCloseOperation(EXIT_ON_CLOSE())

  let button = JButton("Click me!")
  button: setFont(button: getFont(): deriveFont(96.0_F))

  let handler = |event| -> println("Clicked!")
  button: addActionListener(asInterfaceInstance(ActionListener.class, handler))

  frame: getContentPane(): add(button)
  frame: pack()
  frame: setVisible(true)
}

