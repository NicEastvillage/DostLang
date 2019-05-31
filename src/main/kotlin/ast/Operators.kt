package dk.eastvillage.dost.ast


open class Operator(val spelling: String)
class ArithmeticOperator(spelling: String) : Operator(spelling)
class EqualityOperator(spelling: String) : Operator(spelling)
class ComparisonOperator(spelling: String) : Operator(spelling)
class LogicOperator(spelling: String) : Operator(spelling)

object Operators {
    val ADD = ArithmeticOperator("+")
    val SUB = ArithmeticOperator("-")
    val MUL = ArithmeticOperator("*")
    val DIV = ArithmeticOperator("/")
    val MOD = ArithmeticOperator("%")

    val EQ = EqualityOperator("==")
    val NEQ = EqualityOperator("!=")

    val GT = ComparisonOperator(">")
    val LT = ComparisonOperator("<")
    val GEQ = ComparisonOperator(">=")
    val LEQ = ComparisonOperator("<=")

    val AND = LogicOperator("&&")
    val OR = LogicOperator("||")
}