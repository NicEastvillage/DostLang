package dk.eastvillage.dost.contextual

import dk.eastvillage.dost.CompileError
import dk.eastvillage.dost.ErrorLog
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


fun generalizeTypes(type1: Type, type2: Type): Type? {
    return when {
        type1 == type2 -> type1
        type1 == FloatType && type2 == IntegerType -> FloatType
        type2 == FloatType && type1 == IntegerType -> FloatType
        type1 == ErrorType || type2 == ErrorType -> ErrorType
        else -> null
    }
}

fun convertExpr(expr: Expr, type: Type): Expr {
    return when {
        expr.type == type -> expr
        expr.type == IntegerType && type == FloatType -> IntToFloatConversion(expr)
        type == ErrorType -> expr
        else -> {
            ErrorLog += ImplicitConversionError(expr, type)
            expr
        }
    }
}


class ImplicitConversionError(expr: Expr, type: Type) :
    CompileError(expr.sctx, "Cannot convert expression of type ${expr.type.name} to type ${type.name} implicitly.")