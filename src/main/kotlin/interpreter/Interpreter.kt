package dk.eastvillage.dost.interpreter

import dk.eastvillage.dost.CompilationInfo
import dk.eastvillage.dost.ast.*
import dk.eastvillage.dost.ast.ForLoopStepDirection.*
import dk.eastvillage.dost.ast.Operators.ADD
import dk.eastvillage.dost.ast.Operators.AND
import dk.eastvillage.dost.ast.Operators.DIV
import dk.eastvillage.dost.ast.Operators.EQ
import dk.eastvillage.dost.ast.Operators.GEQ
import dk.eastvillage.dost.ast.Operators.GT
import dk.eastvillage.dost.ast.Operators.LEQ
import dk.eastvillage.dost.ast.Operators.LT
import dk.eastvillage.dost.ast.Operators.MOD
import dk.eastvillage.dost.ast.Operators.MUL
import dk.eastvillage.dost.ast.Operators.NEQ
import dk.eastvillage.dost.ast.Operators.OR
import dk.eastvillage.dost.ast.Operators.SUB
import dk.eastvillage.dost.contextual.FloatType
import dk.eastvillage.dost.contextual.IntegerType
import dk.eastvillage.dost.contextual.StringType


open class InterpretRuntimeException(msg: String) : RuntimeException(msg)

class InterpreterVisitException(node: Any) : InterpretRuntimeException("${node.javaClass} should not be visited.")


object RuntimeErrorValue

class Interpreter(
    private val info: CompilationInfo
) : BaseVisitor<Unit, Any>(RuntimeErrorValue) {

    private val stack: Memory = Memory()

    fun start(startNode: Node) {
        stack.pushStack()
        stack.openScope()
        visit(startNode, Unit)
        stack.closeScope()
        stack.popStack()
    }

    override fun visit(node: StmtBlock, data: Unit): Any {
        stack.openScope()
        for (stmt in node.stmts) {
            visit(stmt, Unit)
        }
        stack.closeScope()
        return Unit
    }

    override fun visit(node: VariableDecl, data: Unit): Any {
        val value = visit(node.expr, Unit)
        stack.declare(node.variable.spelling, value)
        return Unit
    }

    override fun visit(node: Assignment, data: Unit): Any {
        val value = visit(node.expr, Unit)
        stack[node.variable.spelling] = value
        return Unit
    }

    override fun visit(node: IfStmt, data: Unit): Any {
        val condition = visit(node.condition, Unit) as Boolean
        if (condition) {
            visit(node.trueBlock, Unit)
        } else {
            node.falseBlock?.let { visit(it, Unit) }
        }
        return Unit
    }

    override fun visit(node: ForLoop, data: Unit): Any {
        val init = visit(node.initExpr, Unit) as Int
        val end = visit(node.endExpr, Unit) as Int
        val range = when (node.stepDirection) {
            UP_TO -> init until end
            UP_TO_INC -> init .. end
            DOWN_TO -> init downTo end
            DOWN_TO_INC -> init downTo end - 1
        }
        val varName = node.variable.spelling

        stack.openScope() // The index variable lives in its own scope
        stack.declare(varName, init)
        for (i in range) {
            stack[varName] = i
            visit(node.block, Unit)
        }
        stack.closeScope()
        return Unit
    }

    override fun visit(node: WhileLoop, data: Unit): Any {
        var condition = visit(node.condition, Unit) as Boolean
        while (condition) {
            visit(node.block, Unit)
            condition = visit(node.condition, Unit) as Boolean
        }
        return Unit
    }

    override fun visit(node: PrintStmt, data: Unit): Any {
        if (node.expr.type == StringType) {
            val str = (visit(node.expr, Unit) as String)
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\\", "\\")
            info.settings.stdout.println(str)
        } else {
            info.settings.stdout.println(visit(node.expr, Unit))
        }
        return Unit
    }

    override fun visit(node: Identifier, data: Unit): Any {
        return stack[node.spelling]
    }

    override fun visit(node: IntLiteral, data: Unit): Any {
        return node.value
    }

    override fun visit(node: FloatLiteral, data: Unit): Any {
        return node.value
    }

    override fun visit(node: BoolLiteral, data: Unit): Any {
        return node.value
    }

    override fun visit(node: StringLiteral, data: Unit): Any {
        return node.value
    }

    override fun visit(node: BinaryExpr, data: Unit): Any {
        // Short-circuit logic operators
        if (node.operator is LogicOperator) {
            when (node.operator) {
                AND -> return (visit(node.left, Unit) as Boolean) && (visit(node.right, Unit) as Boolean)
                OR -> return (visit(node.left, Unit) as Boolean) || (visit(node.right, Unit) as Boolean)
            }
        }

        // All non-short-circuiting binary expressions are left-associative, so we can this first without problems
        val lv = visit(node.left, Unit)
        val rv = visit(node.right, Unit)

        when (node.operator) {
            is ArithmeticOperator -> when (node.operator) {
                ADD -> when (node.type) {
                    IntegerType -> return (lv as Int) + (rv as Int)
                    FloatType -> return (lv as Float) + (rv as Float)
                    StringType -> return (lv as String) + (rv as String)
                }
                SUB -> when (node.type) {
                    IntegerType -> return (lv as Int) - (rv as Int)
                    FloatType -> return (lv as Float) - (rv as Float)
                }
                MUL -> when (node.type) {
                    IntegerType -> return (lv as Int) * (rv as Int)
                    FloatType -> return (lv as Float) * (rv as Float)
                }
                DIV -> when (node.type) {
                    IntegerType -> return (lv as Int) / (rv as Int)
                    FloatType -> return (lv as Float) / (rv as Float)
                }
                MOD -> when (node.type) {
                    IntegerType -> return (lv as Int) % (rv as Int)
                    FloatType -> return (lv as Float) % (rv as Float)
                }
            }
            is ComparisonOperator -> when (node.operator) {
                LT -> when (node.left.type) {
                    IntegerType -> return (lv as Int) < (rv as Int)
                    FloatType -> return (lv as Float) < (rv as Float)
                }
                LEQ -> when (node.left.type) {
                    IntegerType -> return (lv as Int) <= (rv as Int)
                    FloatType -> return (lv as Float) <= (rv as Float)
                }
                GT -> when (node.left.type) {
                    IntegerType -> return (lv as Int) > (rv as Int)
                    FloatType -> return (lv as Float) > (rv as Float)
                }
                GEQ -> when (node.left.type) {
                    IntegerType -> return (lv as Int) >= (rv as Int)
                    FloatType -> return (lv as Float) >= (rv as Float)
                }
            }
            is EqualityOperator -> when (node.operator) {
                EQ -> return lv == rv
                NEQ -> return lv != rv
            }
        }

        throw InterpretRuntimeException("Unknown operator found: '${node.operator.spelling}' for type ${node.type}")
    }

    override fun visit(node: NotExpr, data: Unit): Any {
        return !(visit(node.expr, Unit) as Boolean)
    }

    override fun visit(node: Negation, data: Unit): Any {
        return if (node.type == IntegerType) -(visit(node.expr, Unit) as Int)
        else -(visit(node.expr, Unit) as Float)
    }

    override fun visit(node: IntToFloatConversion, data: Unit): Any {
        return (visit(node.expr, Unit) as Int).toFloat()
    }

    override fun visit(node: AnyToStringConversion, data: Unit): Any {
        return visit(node.expr, Unit).toString()
    }
}