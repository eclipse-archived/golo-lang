module multistrings

function test = -> """Waooo
this
  is
    just
      awesome!"""

function nasty = -> """
This is a multiline string
  with a nasty sequence: \"""
Did it break the parser?
"""