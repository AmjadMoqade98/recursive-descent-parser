this project is an implementation for Recursive Descent Parser against the given Syntax:

  program ->    body   $
  body  ->  lib-decl       main ()       declarations      block
  lib-decl  ->  (  # include < name >   ;   )
  declarations   -> const-decl       var-decl
  const-decl  -> (  const   data-type   name   =    value   ;   )
  var-decl   ->  (  var    data-type    name-list   ;   )
  name-list  ->   name   (  ,   name  )
  data-type  ->   int     |       float
  name  ->     “user-defined-name”
  block  ->  {    stmt-list    }
  stmt-list ->     statement   (  ;     statement   )
  statement -> ass-stmt     |     inout-stmt    |      if-stmt     |    while-stmt   |    block  |
  ass-stmt -> name     =      exp
  exp -> term      (  add-oper   term  )
  term -> factor   (  mul-oper    factor   )
  factor ->  (     exp     )     |     name     |     value
  value ->  “float-number”   |        “int-number”
  add-sign ->  +    |   -
  mul-sign ->     |    /   |   %
  inout-stmt ->code    >>    name         |    output     <<    name-value
  if-stmt -> if   (   bool-exp  )  statement     else-part     endif
  else-part ->  else     statement   |   
  while-stmt -> while   (   bool-exp    )   {    stmt-list    }
  bool-exp -> name-value       relational-oper        name-vaue
  name-value ->  name    |      value
  relational-oper ->  ==      |       !=         |     <     |       <=     |     >     |     >=

the app will check a given input code agains this syntax and return if the code syntax is valid or not. 
if the code were not valid the app will print the found errors with their line number.
