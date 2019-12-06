----
Module containing a macro
----
module docpackage.MyMacros

import gololang.ir.Quote

----
This is a macro
----
macro foo = -> &quote(42)
