package dk.eastvillage.dost.contextual

import dk.eastvillage.dost.CompileError
import dk.eastvillage.dost.ErrorLog
import dk.eastvillage.dost.ast.AnyToStringConversion
import dk.eastvillage.dost.ast.Expr
import dk.eastvillage.dost.ast.IntToFloatConversion


sealed class Type(val name: String)

// Primitives

object UncheckedType : Type("unchecked")
object ErrorType : Type("error-type")
object VoidType : Type("void")
object IntegerType : Type("int")
object FloatType : Type("float")
object BoolType : Type("bool")
object StringType : Type("string")


fun generalizeTypes(type1: Type, type2: Type): Type? {
    return when {
        type1 == ErrorType || type2 == ErrorType -> ErrorType
        type1 == type2 -> type1
        type1 == FloatType && type2 == IntegerType -> FloatType
        type2 == FloatType && type1 == IntegerType -> FloatType
        type1 == StringType || type2 == StringType -> StringType
        else -> null
    }
}


class ImplicitConversionError(expr: Expr, type: Type) :
    CompileError(expr.sctx, "Cannot convert expression of type ${expr.type.name} to type ${type.name} implicitly.")