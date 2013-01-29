module test

import java.awt.event
import javax.swing
import javax.swing.WindowConstants

local function listener = |handler| -> asInterfaceInstance(ActionListener.class, handler)

function main = |args| {

  let frame = JFrame("Action listeners")
  frame: setDefaultCloseOperation(EXIT_ON_CLOSE())

  let button = JButton("Click me!")
  button: setFont(button: getFont(): deriveFont(96.0_F))
  button: addActionListener(listener(|event| -> println("Clicked!")))

  # Using a standard pimp: MethodHandle::to(Class)
  button: addActionListener((|event| -> println("[click]")): to(ActionListener.class))

  frame: getContentPane(): add(button)
  frame: pack()
  frame: setVisible(true)
}

