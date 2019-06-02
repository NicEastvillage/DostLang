package dk.eastvillage.dost.interpreter


import java.util.*
import kotlin.collections.HashMap

class UndeclaredSymbol(symbol: String) : InterpretRuntimeException("'$symbol' was undefined!")
class RedeclaredSymbol(symbol: String) : InterpretRuntimeException("'$symbol' already declared!")

/**
 * The MemoryStack represents a combination of block scopes and the process stack. It is used to map identifiers to
 * values. The scope and stack is controlled with open/close scope and push/pop stack functions. Stacks are created
 * by function calls, and the code in a stack frame cannot see identifiers from other stack frames. Scopes are created
 * by blocks, and the code in a block can see the values of scopes surrounding it.
 */
class MemoryStack<I, V> {

    // The actual data structures of the MemoryStack
    private class ValueTable<I, V> : HashMap<I, V>()
    private class NestedScope<I, V> : Stack<ValueTable<I, V>>()
    private val stack = Stack<NestedScope<I, V>>()

    /**
     * Add a new identifier in the current stack/scope
     */
    fun declare(identifier: I, value: V) {
        if (stack.peek().peek().containsKey(identifier)) {
            throw RedeclaredSymbol(identifier.toString())
        }
        stack.peek().peek()[identifier] = value
    }

    /**
     * Change the value associated with the given identifier
     */
    operator fun set(identifier: I, value: V) {
        for (table in stack.peek()) {
            if (table.containsKey(identifier)) {
                table[identifier] = value
                return
            }
        }
        throw UndeclaredSymbol(identifier.toString())
    }

    /**
     * Retrieve the value associated with the given identifier
     */
    operator fun get(identifier: I): V {
        for (table in stack.peek()) {
            table[identifier]?.let { return it }
        }
        throw UndeclaredSymbol(identifier.toString())
    }

    fun pushStack() {
        stack.push(NestedScope())
    }

    fun popStack() {
        stack.pop()
    }

    fun openScope() {
        stack.peek().push(ValueTable())
    }

    fun closeScope() {
        stack.peek().pop()
    }
}