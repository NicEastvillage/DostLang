package dk.eastvillage.dost.ast


interface DostVisitor<D, R> {

    fun visit(node: Node, data: D): R {
        return when (node) {
            is Stmt -> visit(node, data)
            is Expr -> visit(node, data)
            else -> throw AssertionError("Trying to visit unknown NodeWithContext.")
        }
    }

    fun visit(node: Stmt, data: D): R {
        return when (node) {
            is StmtBlock -> visit(node, data)
            is VariableDecl -> visit(node, data)
            is Assignment -> visit(node, data)
            is IfStmt -> visit(node, data)
            is ForLoop -> visit(node, data)
            is WhileLoop -> visit(node, data)
            else -> throw AssertionError("Trying to visit unknown Stmt.")
        }
    }

    fun visit(node: Expr, data: D): R {
        return when (node) {
            is Identifier -> visit(node, data)
            is IntLiteral -> visit(node, data)
            is FloatLiteral -> visit(node, data)
            is BoolLiteral -> visit(node, data)
            is BinaryExpr -> visit(node, data)
            is NotExpr -> visit(node, data)
            is Negation -> visit(node, data)
            else -> throw AssertionError("Trying to visit unknown Expr.")
        }
    }

    fun visit(node: StmtBlock, data: D): R
    fun visit(node: VariableDecl, data: D): R
    fun visit(node: Assignment, data: D): R
    fun visit(node: IfStmt, data: D): R
    fun visit(node: ForLoop, data: D): R
    fun visit(node: WhileLoop, data: D): R

    fun visit(node: Identifier, data: D): R
    fun visit(node: IntLiteral, data: D): R
    fun visit(node: FloatLiteral, data: D): R
    fun visit(node: BoolLiteral, data: D): R
    fun visit(node: BinaryExpr, data: D): R
    fun visit(node: NotExpr, data: D): R
    fun visit(node: Negation, data: D): R
}

abstract class DostBaseVisitor<D, R>(private val defaultValue: R) : DostVisitor<D, R> {

    override fun visit(node: StmtBlock, data: D): R = defaultValue
    override fun visit(node: VariableDecl, data: D): R = defaultValue
    override fun visit(node: Assignment, data: D): R = defaultValue
    override fun visit(node: IfStmt, data: D): R = defaultValue
    override fun visit(node: ForLoop, data: D): R = defaultValue
    override fun visit(node: WhileLoop, data: D): R = defaultValue
    override fun visit(node: Identifier, data: D): R = defaultValue
    override fun visit(node: IntLiteral, data: D): R = defaultValue
    override fun visit(node: FloatLiteral, data: D): R = defaultValue
    override fun visit(node: BoolLiteral, data: D): R = defaultValue
    override fun visit(node: BinaryExpr, data: D): R = defaultValue
    override fun visit(node: NotExpr, data: D): R = defaultValue
    override fun visit(node: Negation, data: D): R = defaultValue
}