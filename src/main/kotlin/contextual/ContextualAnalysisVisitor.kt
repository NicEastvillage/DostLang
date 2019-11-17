package dk.eastvillage.dost.contextual

import dk.eastvillage.dost.*
import dk.eastvillage.dost.ast.*


class UndeclaredIdentError(sctx: SourceContext?, ident: String) :
    CompileError(sctx, "'$ident' has not been declared.")

class NotReassignableError(sctx: SourceContext?, ident: String, decl: Node) :
    CompileError(sctx, "'$ident' is not reassignable since it was declared as a ${decl.javaClass}.")

class NotIndexableError(sctx: SourceContext?, type: Type) :
    CompileError(sctx, "Indexing is not possible for $type.")

open class TypeError(sctx: SourceContext?, description: String) : CompileError(sctx, description)

class IncompatibleTypesError(sctx: SourceContext?, type1: Type, type2: Type) :
    TypeError(sctx, "${type1.name} and ${type2.name} are incompatible types.")

class ArithmeticTypeError(binaryExpr: BinaryExpr) :
    TypeError(binaryExpr.sctx, "Cannot do arithmetic operation '${binaryExpr.operator.spelling}' " +
            "on ${binaryExpr.left.type} and ${binaryExpr.right.type}." +
            "Operands must be ${IntegerType.name}s or ${FloatType.name}s.")

class LogicTypeError(binaryExpr: BinaryExpr) :
    TypeError(binaryExpr.sctx, "Cannot do local operation '${binaryExpr.operator.spelling}' " +
            "on ${binaryExpr.left.type} and ${binaryExpr.right.type}. Operands must be ${BoolType.name}s.")


@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class ContextualAnalysisVisitor(
    private val info: CompilationInfo
) : BaseVisitor<Unit, Unit>(Unit) {

    val identTable: IdentTable = IdentTable()

    fun analyse(node: Node) {
        visit(node, Unit)
    }

    private fun convertExpr(expr: Expr, type: Type): Expr {
        return when {
            expr.type == type -> expr
            expr.type == IntegerType && type == FloatType -> IntToFloatConversion(expr)
            type == StringType -> AnyToStringConversion(expr) // All values can be converted to a string
            type == ErrorType -> expr
            else -> {
                info.errors += ImplicitConversionError(expr, type)
                expr
            }
        }
    }

    override fun visit(node: StmtBlock, data: Unit) {
        identTable.openScope()
        for (stmt in node.stmts) {
            visit(stmt, Unit)
        }
        identTable.closeScope()
    }

    override fun visit(node: VariableDecl, data: Unit) {
        visit(node.expr, Unit)
        node.inferredType = node.expr.type
        try {
            identTable[node.variable.spelling] = node
        } catch (e: RedeclarationException) {
            info.errors += e.error
        }
    }

    override fun visit(node: Assignment, data: Unit) {
        visit(node.lvalue, Unit)
        visit(node.expr, Unit)
        node.expr = convertExpr(node.expr, node.lvalue.type)
    }

    override fun visit(node: IfStmt, data: Unit) {
        visit(node.condition, Unit)
        node.condition = convertExpr(node.condition, BoolType)
        visit(node.trueBlock, Unit)
        node.falseBlock?.let { visit(it, Unit) }
    }

    override fun visit(node: ForLoop, data: Unit) {
        visit(node.initExpr, Unit)
        node.initExpr = convertExpr(node.initExpr, IntegerType)
        visit(node.endExpr, Unit)
        node.endExpr = convertExpr(node.endExpr, IntegerType)

        identTable.openScope()
        try {
            identTable[node.variable.spelling] = node
        } catch (e: RedeclarationException) {
            info.errors += e.error
        }
        visit(node.block, Unit)
        identTable.closeScope()
    }

    override fun visit(node: WhileLoop, data: Unit) {
        visit(node.condition, Unit)
        node.condition = convertExpr(node.condition, BoolType)
        visit(node.block, Unit)
    }

    override fun visit(node: PrintStmt, data: Unit) {
        visit(node.expr, Unit)
        if (node.expr.type !is StringType) {
            node.expr = AnyToStringConversion(node.expr)
        }
    }

    override fun visit(node: Identifier, data: Unit) {
        val decl = identTable[node.spelling]
        node.type = when (decl) {
            is VariableDecl -> decl.inferredType
            is ForLoop -> IntegerType
            null -> {
                info.errors += UndeclaredIdentError(node.sctx, node.spelling)
                ErrorType
            }
            else -> throw TerminatedCompilationException("Identifier referred to unknown or illegal declaration: ${decl.javaClass}")
        }
    }

    override fun visit(node: BinaryExpr, data: Unit) {
        visit(node.left, Unit)
        visit(node.right, Unit)

        val type = generalizeTypes(node.left.type, node.right.type)

        // Bad types
        if (type == null) {
            info.errors += IncompatibleTypesError(node.sctx, node.left.type, node.right.type)
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
                    info.errors += ArithmeticTypeError(node)
                    node.type = ErrorType
                }
            }
            is ComparisonOperator -> {
                if (type != FloatType && type != IntegerType) {
                    info.errors += ArithmeticTypeError(node)
                    node.type = ErrorType
                } else {
                    node.left = convertExpr(node.left, type)
                    node.right = convertExpr(node.right, type)
                    node.type = BoolType
                }
            }
            is LogicOperator -> {
                if (type != BoolType) {
                    info.errors += LogicTypeError(node)
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

    override fun visit(node: NotExpr, data: Unit) {
        visit(node.expr, Unit)
        convertExpr(node.expr, BoolType)
        node.type = BoolType
    }

    override fun visit(node: Negation, data: Unit) {
        visit(node.expr, Unit)
        when (node.expr.type) {
            IntegerType, FloatType, ErrorType -> {
                node.type = node.expr.type
            }
            else -> {
                info.errors += TypeError(node.sctx, "Cannot negate expression of type ${node.expr.type}.")
                node.type = ErrorType
            }
        }
    }

    override fun visit(node: ArrayLiteral, data: Unit) {
        visit(node.sizeExpr, data)
        if (node.sizeExpr.type != IntegerType) {
            info.errors += TypeError(node.sizeExpr.sctx, "Size of array must be an integer.")
        }
        node.type = ArrayType(node.declaredType)
    }

    override fun visit(node: IndexAccessExpr, data: Unit) {
        visit(node.arrayExpr, data)
        val arrayType = node.arrayExpr.type
        if (arrayType !is ArrayType) {
            info.errors += TypeError(node.arrayExpr.sctx, "Index accessing can only be done on arrays.")
            node.type = ErrorType
        } else {
            node.type = arrayType.subtype
        }

        visit(node.indexExpr, data)
        if (node.indexExpr.type != IntegerType) {
            info.errors += TypeError(node.indexExpr.sctx, "Index expression must be an integer.")
        }
    }

    override fun visit(node: IntToFloatConversion, data: Unit) {
        visit(node.expr, Unit)
        if (node.expr.type != IntegerType && node.expr.type != ErrorType) {
            info.errors += TypeError(node.sctx, "Cannot convert expression of type ${node.expr.type} to type ${FloatType.name}.")
        }
        node.type = FloatType
    }

    override fun visit(node: AnyToStringConversion, data: Unit) {
        // All values can be converted to a string
        visit(node.expr, Unit)
        node.type = StringType
    }

    override fun visit(node: LValueVariable, data: Unit) {
        val decl = identTable[node.variable.spelling]
        when (decl) {
            null -> info.errors += UndeclaredIdentError(node.variable.sctx, node.variable.spelling)
            is VariableDecl -> {
                node.type = decl.inferredType
            }
            else -> info.errors += NotReassignableError(node.variable.sctx, node.variable.spelling, decl)
        }
    }

    override fun visit(node: LValueIndexing, data: Unit) {
        visit(node.lvalue, Unit)
        when (node.lvalue.type) {
            is ArrayType -> node.type = (node.lvalue.type as ArrayType).subtype
            else -> info.errors += NotIndexableError(node.sctx, node.type)
        }

        visit(node.expr, Unit)
        if (node.expr.type != IntegerType) {
            info.errors += TypeError(node.sctx, "Index expression must be of type ${IntegerType.name}.")
        }
    }
}