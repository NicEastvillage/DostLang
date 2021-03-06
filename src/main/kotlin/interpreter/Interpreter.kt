package dk.eastvillage.dost.interpreter

import dk.eastvillage.dost.CompilationInfo
import dk.eastvillage.dost.SourceContext
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
import dk.eastvillage.dost.contextual.*
import kotlin.AssertionError


/** Exception thrown by the interpreter when something is wrong in the interpreter */
open class InterpreterException(msg: String) : RuntimeException(msg)

class InterpreterVisitException(node: Any) : InterpreterException("${node.javaClass} should not be visited.")

/** Errors that happen at runtime while executing the user's script */
open class ExecutionException(val sctx: SourceContext?, val mgs: String) : RuntimeException()

class OutOfBoundsException(sctx: SourceContext?, index: Int, size: Int) : ExecutionException(
    sctx,
    "Index out of bounds. Index was $index but size of array was $size."
)

object RuntimeErrorValue

class Interpreter(
    private val info: CompilationInfo
) : BaseVisitor<Unit, Any>(RuntimeErrorValue) {

    private val memory: Memory = Memory()

    fun start(startNode: Node) {
        try {
            memory.pushStack()
            memory.openScope()
            visit(startNode, Unit)
            memory.closeScope()
            memory.popStack()
        } catch (e: ExecutionException) {
            if (e.sctx == null) {
                info.settings.stderr.println("RuntimeError: ${e.mgs}")
            } else {
                info.settings.stdout.println("RuntimeError at ${e.sctx.position()}: ${e.mgs}")
            }
        }
    }

    override fun visit(node: StmtBlock, data: Unit): Any {
        memory.openScope()
        for (stmt in node.stmts) {
            visit(stmt, Unit)
        }
        memory.closeScope()
        return Unit
    }

    override fun visit(node: VariableDecl, data: Unit): Any {
        val value = visit(node.expr, Unit)
        memory.declare(node.variable.spelling, value)
        return Unit
    }

    override fun visit(node: Assignment, data: Unit): Any {
        val rvalue = visit(node.expr, Unit)
        val lvalue = node.lvalue

        if (lvalue is LValueIndexing) {
            // An array is a pointer to a chunk of memory.
            // When indexing, we need to find the base address and then add the offset
            fun getAddressOfLValue(lvi: LValue): Int = when (lvi) {
                is LValueIndexing -> {
                    val subAddr = getAddressOfLValue(lvi.lvalue)
                    val addr = if (lvi.lvalue is LValueIndexing) {
                        // Follow the pointer if the sub-lvalue was also an LValueIndexing
                        memory[subAddr] as Int
                    } else {
                        subAddr
                    }
                    val offset = visit(lvi.expr, Unit) as Int
                    // First value of the array is the size
                    val size = memory[addr] as Int
                    if (offset < 0 || size <= offset) {
                        throw OutOfBoundsException(lvi.sctx, offset, size)
                    }
                    addr + offset + 1
                }
                is LValueVariable -> (memory.get(lvi.variable.spelling) as ArrayPointer).addrToSize // Get address of array (pointer)
                else -> throw AssertionError("Unknown LValue.")
            }
            val addr = getAddressOfLValue(lvalue)
            memory[addr] = rvalue

        } else if (lvalue is LValueVariable) {
            memory.set(lvalue.variable.spelling, rvalue)
        }


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

        memory.openScope() // The index variable lives in its own scope
        memory.declare(varName, init)
        for (i in range) {
            memory.set(varName, i)
            visit(node.block, Unit)
        }
        memory.closeScope()
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
        return memory.get(node.spelling)
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

        throw InterpreterException("Unknown operator found: '${node.operator.spelling}' for type ${node.type}")
    }

    override fun visit(node: NotExpr, data: Unit): Any {
        return !(visit(node.expr, Unit) as Boolean)
    }

    override fun visit(node: Negation, data: Unit): Any {
        return if (node.type == IntegerType) -(visit(node.expr, Unit) as Int)
        else -(visit(node.expr, Unit) as Float)
    }

    override fun visit(node: IndexAccessExpr, data: Unit): Any {
        val array = visit(node.arrayExpr, data) as ArrayPointer
        val index = visit(node.indexExpr, data) as Int
        val size = memory[array.addrToSize] as Int
        if (index < 0 || size <= index) {
            throw OutOfBoundsException(node.sctx, index, size)
        }
        return memory[array.addrToValues + index]
    }

    override fun visit(node: ArrayLiteral, data: Unit): Any {
        val size = visit(node.sizeExpr, Unit) as Int
        // Allocate a chunk that is +1 of size. The first value will contain the size
        val chunk = memory.allocChunk(size + 1)
        val array = ArrayPointer(chunk, chunk + 1)
        memory[array.addrToSize] = size
        // Fill array with default value
        val defaultValue = arrayDefaultValue((node.type as ArrayType).subtype)
        for (i in 0 until size) {
            memory[array.addrToValues + i] = defaultValue
        }
        return array
    }

    private fun arrayDefaultValue(type: Type): Any {
        return when (type) {
            IntegerType -> 0
            FloatType -> 0f
            BoolType -> false
            StringType -> ""
            is ArrayType -> throw NotImplementedError("Default values for arrays within arrays are unimplemented.") //TODO
            else -> throw AssertionError("${type.name} has no default value.")
        }
    }

    override fun visit(node: IntToFloatConversion, data: Unit): Any {
        return (visit(node.expr, Unit) as Int).toFloat()
    }

    override fun visit(node: AnyToStringConversion, data: Unit): Any {
        val value = visit(node.expr, Unit)
        return stringRepresentation(value)
    }

    private fun stringRepresentation(value: Any): String {
        return when (value) {
            is ArrayPointer -> {
                val sb = StringBuilder("[")
                val size = memory[value.addrToSize] as Int
                for (i in 0 until size) {
                    if (i > 0) sb.append(", ")
                    sb.append(stringRepresentation(memory[value.addrToValues + i]))
                }
                sb.append("]")
                sb.toString()
            }
            is String -> "\"$value\""
            else -> value.toString()
        }
    }
}