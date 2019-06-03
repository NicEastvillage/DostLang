package dk.eastvillage.dost.contextual

import dk.eastvillage.dost.CompileError
import dk.eastvillage.dost.ErrorLog
import dk.eastvillage.dost.SourceContext
import dk.eastvillage.dost.TerminatedCompilationException
import dk.eastvillage.dost.ast.*


class UndeclaredIdentError(sctx: SourceContext?, ident: String) :
    CompileError(sctx, "'$ident' has not been declared.")

class NotReassignableError(sctx: SourceContext?, ident: String, decl: Node) :
    CompileError(sctx, "'$ident' is not reassignable since it was declared as a ${decl.javaClass}.")

open class TypeError(sctx: SourceContext?, description: String) : CompileError(sctx, description)

class IncompatibleTypesError(sctx: SourceContext?, type1: Type, type2: Type) :
    TypeError(sctx, "${type1.name} and $type2 are incompatible types.")

class ArithmeticTypeError(binaryExpr: BinaryExpr) :
    TypeError(binaryExpr.sctx, "Cannot do arithmetic operation '${binaryExpr.operator.spelling}' " +
            "on ${binaryExpr.left.type} and ${binaryExpr.right.type}." +
            "Operands must be ${IntegerType.name}s or ${FloatType.name}s.")

class LogicTypeError(binaryExpr: BinaryExpr) :
    TypeError(binaryExpr.sctx, "Cannot do local operation '${binaryExpr.operator.spelling}' " +
            "on ${binaryExpr.left.type} and ${binaryExpr.right.type}. Operands must be ${BoolType.name}s.")


@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object ContextualAnalysisVisitor : BaseVisitor<ContextualAnalysisVisitor.Context, Unit>(Unit) {

    class Context(
        val identTable: IdentTable = IdentTable()
    )

    fun analyse(node: Node) {
        val ctx = Context()
        visit(node, ctx)
    }

    override fun visit(node: StmtBlock, ctx: Context) {
        ctx.identTable.openScope()
        for (stmt in node.stmts) {
            visit(stmt, ctx)
        }
        ctx.identTable.closeScope()
    }

    override fun visit(node: VariableDecl, ctx: Context) {
        ctx.identTable[node.variable.spelling] = node
        visit(node.expr, ctx)
        node.inferredType = node.expr.type
    }

    override fun visit(node: Assignment, ctx: Context) {
        visit(node.expr, ctx)
        val decl = ctx.identTable[node.variable.spelling]
        when (decl) {
            null -> ErrorLog += UndeclaredIdentError(node.variable.sctx, node.variable.spelling)
            is VariableDecl -> {
                node.expr = convertExpr(node.expr, decl.inferredType)
            }
            else -> ErrorLog += NotReassignableError(node.variable.sctx, node.variable.spelling, decl)
        }
    }

    override fun visit(node: IfStmt, ctx: Context) {
        visit(node.condition, ctx)
        node.condition = convertExpr(node.condition, BoolType)
        visit(node.trueBlock, ctx)
        node.falseBlock?.let { visit(it, ctx) }
    }

    override fun visit(node: ForLoop, ctx: Context) {
        visit(node.initExpr, ctx)
        node.initExpr = convertExpr(node.initExpr, IntegerType)
        visit(node.endExpr, ctx)
        node.endExpr = convertExpr(node.endExpr, IntegerType)

        ctx.identTable.openScope()
        ctx.identTable[node.variable.spelling] = node
        visit(node.block, ctx)
        ctx.identTable.closeScope()
    }

    override fun visit(node: WhileLoop, ctx: Context) {
        visit(node.condition, ctx)
        node.condition = convertExpr(node.condition, BoolType)
        visit(node.block, ctx)
    }

    override fun visit(node: PrintStmt, ctx: Context) {
        visit(node.expr, ctx)
    }

    override fun visit(node: Identifier, ctx: Context) {
        val decl = ctx.identTable[node.spelling]
        node.type = when (decl) {
            is VariableDecl -> decl.inferredType
            is ForLoop -> IntegerType
            null -> {
                ErrorLog += UndeclaredIdentError(node.sctx, node.spelling)
                ErrorType
            }
            else -> throw TerminatedCompilationException("Identifier referred to unknown or illegal declaration: ${decl.javaClass}")
        }
    }

    override fun visit(node: BinaryExpr, ctx: Context) {
        visit(node.left, ctx)
        visit(node.right, ctx)

        val type = generalizeTypes(node.left.type, node.right.type)

        // Bad types
        if (type == null) {
            ErrorLog += IncompatibleTypesError(node.sctx, node.left.type, node.right.type)
            // All binary expressions except arithmetic returns bool. We will do that, even if there's an error
            if (node.operator is ArithmeticOperator) {
                node.type = ErrorType
            } else {
                node.type = BoolType
            }
            return
        } else if (type == ErrorType) {
            node.type = ErrorType
            return
        }

        when (node.operator) {
            is ArithmeticOperator -> {
                // Note addition is allowed for strings as concatenation
                if (type == FloatType || type == IntegerType || (node.operator == Operators.ADD && type == StringType)) {
                    node.left = convertExpr(node.left, type)
                    node.right = convertExpr(node.right, type)
                    node.type = type
                } else {
                    ErrorLog += ArithmeticTypeError(node)
                    node.type = ErrorType
                }
            }
            is ComparisonOperator -> {
                if (type != FloatType && type != IntegerType) {
                    ErrorLog += ArithmeticTypeError(node)
                    node.type = ErrorType
                } else {
                    node.left = convertExpr(node.left, type)
                    node.right = convertExpr(node.right, type)
                    node.type = BoolType
                }
            }
            is LogicOperator -> {
                if (type != BoolType) {
                    ErrorLog += LogicTypeError(node)
                    node.type = ErrorType
                } else {
                    node.left = convertExpr(node.left, BoolType)
                    node.right = convertExpr(node.right, BoolType)
                    node.type = BoolType
                }
            }
            is EqualityOperator -> {
                // Types passed generalization so things must be ok
                node.left = convertExpr(node.left, type)
                node.right = convertExpr(node.right, type)
                node.type = BoolType
            }
            else -> throw TerminatedCompilationException("Unknown operator: ${node.operator}.")
        }
    }

    override fun visit(node: NotExpr, ctx: Context) {
        visit(node.expr, ctx)
        convertExpr(node.expr, BoolType)
        node.type = BoolType
    }

    override fun visit(node: Negation, ctx: Context) {
        visit(node.expr, ctx)
        when (node.expr.type) {
            IntegerType, FloatType, ErrorType -> {
                node.type = node.expr.type
            }
            else -> {
                ErrorLog += TypeError(node.sctx, "Cannot negate expression of type ${node.expr.type}.")
                node.type = ErrorType
            }
        }
    }

    override fun visit(node: IntToFloatConversion, ctx: Context) {
        visit(node.expr, ctx)
        if (node.expr.type != IntegerType && node.expr.type != ErrorType) {
            ErrorLog += TypeError(node.sctx, "Cannot convert expression of type ${node.expr.type} to type ${FloatType.name}.")
        }
        node.type = FloatType
    }

    override fun visit(node: AnyToStringConversion, ctx: Context) {
        // All values can be converted to a string
        visit(node.expr, ctx)
        node.type = StringType
    }
}