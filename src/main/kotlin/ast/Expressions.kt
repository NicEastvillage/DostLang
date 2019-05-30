package dk.eastvillage.dost.ast

import dk.eastvillage.dost.*


class Identifier(
    ctx: SourceContext,
    type: Type = UncheckedType
) : Expr(ctx, type)

class IntLiteral(
    ctx: SourceContext,
    type: Type = IntegerType
) : Expr(ctx, type)

class FloatLiteral(
    ctx: SourceContext,
    type: Type = FloatType
) : Expr(ctx, type)

class BoolLiteral(
    ctx: SourceContext,
    type: Type = BooleanType
) : Expr(ctx, type)

class BinaryExpr(
    ctx: SourceContext,
    type: Type = UncheckedType,
    var left: Node,
    var right: Node,
    val operator: Operator
) : Expr(ctx, type)

sealed class Operator(val leftToRightAssociative: Boolean)
sealed class ArithmeticOperator(leftToRightAssociative: Boolean) : Operator(leftToRightAssociative)
sealed class EqualityOperator(leftToRightAssociative: Boolean) : Operator(leftToRightAssociative)
sealed class ComparisonOperator(leftToRightAssociative: Boolean) : Operator(leftToRightAssociative)
sealed class LogicOperator(leftToRightAssociative: Boolean) : Operator(leftToRightAssociative)

object ADD : ArithmeticOperator(true)
object SUB : ArithmeticOperator(true)
object MUL : ArithmeticOperator(true)
object DIV : ArithmeticOperator(true)
object MOD : ArithmeticOperator(true)

object GT : ComparisonOperator(true)
object LT : ComparisonOperator(true)
object LEQ : ComparisonOperator(true)
object GEQ : ComparisonOperator(true)

object EQ : EqualityOperator(true)
object NEQ : EqualityOperator(true)

object AND : LogicOperator(true)
object OR : LogicOperator(true)

class NotExpr(
    ctx: SourceContext,
    type: Type = BooleanType
) : Expr(ctx, type)

class Negation(
    ctx: SourceContext,
    type: Type = UncheckedType
) : Expr(ctx, type)