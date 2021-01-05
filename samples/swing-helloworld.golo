# Copyright (c) 2012-2021 Institut National des Sciences Appliquées de Lyon (INSA Lyon) and others
#
# All rights reserved. This Example Content is intended to demonstrate
# usage of Eclipse technology. It is provided to you under the terms and
# conditions of the Eclipse Distribution License v1.0 which is available
# at http://www.eclipse.org/org/documents/edl-v10.php

module samples.SwingHelloWorld

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
