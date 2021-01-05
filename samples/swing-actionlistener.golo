# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.SwingActionListener

import java.awt.event
import javax.swing
import javax.swing.WindowConstants

local function listener = |handler| -> asInterfaceInstance(ActionListener.class, handler)

function main = |args| {

  let frame = JFrame("Action listeners")
  frame: setDefaultCloseOperation(EXIT_ON_CLOSE())

  let button = JButton("Click me!")
  button: setFont(button: getFont(): deriveFont(96.0_F))

  # Using a helper function
  button: addActionListener(listener(|event| -> println("Clicked!")))

  # Using a standard augmentation: MethodHandle::to(Class)
  button: addActionListener((|event| -> println("[click]")): to(ActionListener.class))

  # Straight closure passing
  button: addActionListener(|event| -> println("( )"))

  frame: getContentPane(): add(button)
  frame: pack()
  frame: setVisible(true)
}
