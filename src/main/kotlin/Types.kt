package dk.eastvillage.dost

sealed class Type(val name: String)

// Primitives
object UncheckedType : Type("unchecked")
object VoidType : Type("void")
object IntegerType : Type("int")
object FloatType : Type("float")
object BooleanType : Type("bool")
