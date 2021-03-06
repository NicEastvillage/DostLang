package dk.eastvillage.dost.ast


interface Visitor<D, R> {

    fun visit(node: Node, data: D): R {
        return when (node) {
            is Stmt -> visit(node, data)
            is Expr -> visit(node, data)
            is GlobalBlock -> visit(node, data)
            else -> throw AssertionError("Trying to visit unknown NodeWithContext.")
        }
    }

    fun visit(node: GlobalBlock, data: D): R {
        var result = visit(node.stmts[0], data)
        for (i in 1 until node.stmts.size) {
            val newResult = visit(node.stmts[i], data)
            result = aggregateResults(result, newResult)
        }
        return result
    }

    fun aggregateResults(prev: R, new: R): R = new

    fun visit(node: Stmt, data: D): R {
        return when (node) {
            is StmtBlock -> visit(node, data)
            is VariableDecl -> visit(node, data)
            is Assignment -> visit(node, data)
            is IfStmt -> visit(node, data)
            is ForLoop -> visit(node, data)
            is WhileLoop -> visit(node, data)
            is PrintStmt -> visit(node, data)
            else -> throw AssertionError("Trying to visit unknown Stmt.")
        }
    }

    fun visit(node: Expr, data: D): R {
        return when (node) {
            is LValue -> visit(node, data)
            is Identifier -> visit(node, data)
            is IntLiteral -> visit(node, data)
            is FloatLiteral -> visit(node, data)
            is BoolLiteral -> visit(node, data)
            is StringLiteral -> visit(node, data)
            is ArrayLiteral -> visit(node, data)
            is BinaryExpr -> visit(node, data)
            is NotExpr -> visit(node, data)
            is Negation -> visit(node, data)
            is IndexAccessExpr -> visit(node, data)
            is IntToFloatConversion -> visit(node, data)
            is AnyToStringConversion -> visit(node, data)
            else -> throw AssertionError("Trying to visit unknown Expr.")
        }
    }

    fun visit(node: LValue, data: D): R {
        return when (node) {
            is LValueVariable -> visit(node, data)
            is LValueIndexing -> visit(node, data)
            else -> throw AssertionError("Trying to visit unknown LValue.")
        }
    }

    fun visit(node: StmtBlock, data: D): R
    fun visit(node: VariableDecl, data: D): R
    fun visit(node: Assignment, data: D): R
    fun visit(node: LValueVariable, data: D): R
    fun visit(node: LValueIndexing, data: D): R
    fun visit(node: IfStmt, data: D): R
    fun visit(node: ForLoop, data: D): R
    fun visit(node: WhileLoop, data: D): R
    fun visit(node: PrintStmt, data: D): R

    fun visit(node: Identifier, data: D): R
    fun visit(node: IntLiteral, data: D): R
    fun visit(node: FloatLiteral, data: D): R
    fun visit(node: BoolLiteral, data: D): R
    fun visit(node: StringLiteral, data: D): R
    fun visit(node: ArrayLiteral, data: D): R
    fun visit(node: BinaryExpr, data: D): R
    fun visit(node: NotExpr, data: D): R
    fun visit(node: Negation, data: D): R
    fun visit(node: IndexAccessExpr, data: D): R
    fun visit(node: IntToFloatConversion, data: D): R
    fun visit(node: AnyToStringConversion, data: D): R
}

abstract class BaseVisitor<D, R>(private val defaultValue: R) : Visitor<D, R> {

    override fun visit(node: StmtBlock, data: D): R = defaultValue
    override fun visit(node: VariableDecl, data: D): R = defaultValue
    override fun visit(node: Assignment, data: D): R = defaultValue
    override fun visit(node: LValueVariable, data: D): R = defaultValue
    override fun visit(node: LValueIndexing, data: D): R = defaultValue
    override fun visit(node: IfStmt, data: D): R = defaultValue
    override fun visit(node: ForLoop, data: D): R = defaultValue
    override fun visit(node: WhileLoop, data: D): R = defaultValue
    override fun visit(node: PrintStmt, data: D): R = defaultValue
    override fun visit(node: Identifier, data: D): R = defaultValue
    override fun visit(node: IntLiteral, data: D): R = defaultValue
    override fun visit(node: FloatLiteral, data: D): R = defaultValue
    override fun visit(node: BoolLiteral, data: D): R = defaultValue
    override fun visit(node: StringLiteral, data: D): R = defaultValue
    override fun visit(node: ArrayLiteral, data: D): R = defaultValue
    override fun visit(node: BinaryExpr, data: D): R = defaultValue
    override fun visit(node: NotExpr, data: D): R = defaultValue
    override fun visit(node: Negation, data: D): R = defaultValue
    override fun visit(node: IndexAccessExpr, data: D): R = defaultValue
    override fun visit(node: IntToFloatConversion, data: D): R = defaultValue
    override fun visit(node: AnyToStringConversion, data: D): R = defaultValue
}