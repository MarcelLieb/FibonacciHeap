import java.util.*
import kotlin.math.ceil
import kotlin.math.ln

interface Node<E> {
    var parent: Node<E>?
    val children: MutableList<Node<E>>
    var value: E
    var excited: Boolean
}

class NodeImpl<E>(override var value: E) : Node<E> {
    override var parent: Node<E>? = null
    override val children: MutableList<Node<E>> = mutableListOf()
    override var excited: Boolean = false
}

class FibonacciHeap<E : Comparable<E>> : Queue<E> {
    override var size: Int = 0
    private val roots: MutableList<Node<E>> = mutableListOf()
    private var min: Node<E>? = null
    private val nodeLookup: MutableMap<E, MutableList<Node<E>>> = mutableMapOf()

    private fun insert(value: E): Node<E> {
        val node = NodeImpl(value)
        roots.add(node)
        if (min == null || node.value < min!!.value) {
            min = node
        }
        nodeLookup.getOrPut(value) { mutableListOf() }.add(node)
        size++
        return node
    }

    fun decreaseKey(entry: E, value: E): Boolean {
        if (value >= entry)
            return false
        if (!nodeLookup.containsKey(entry))
            return false
        val list = nodeLookup[entry]!!
        val node = list.find { it.value == entry } ?: return false
        decreaseKey(node, value)
        return true
    }

    private fun decreaseKey(node: Node<E>, value: E) {
        node.value = value
        //Needs to be less than or equal for delete to work
        if (node.parent != null && node.value <= node.parent!!.value) {
            cut(node)
        }
        if (node.value <= min!!.value) {
            min = node
        }
    }

    private fun cut(node: Node<E>) {
        if (node.parent == null) {
            throw Exception("Trying to cut a root")
        }
        node.parent!!.children.remove(node)
        val parent = node.parent!!
        node.parent = null
        node.excited = false
        roots.add(node)
        if (parent.excited) {
            parent.excited = false
            cut(parent)
        } else {
            if (parent.parent != null) {
                parent.excited = true
            }
        }
    }

    private fun deleteMin(): Node<E>? {
        if (isEmpty())
            return null

        val min = min!!
        roots.remove(min)
        min.children.onEach { it.parent = null }
        roots.addAll(min.children)
        if (roots.isEmpty()) {
            this.min = null
        } else {
            consolidate()
            this.min = roots.minBy { it.value }
        }
        size--

        nodeLookup[min.value]!!.remove(min)
        if (nodeLookup[min.value]!!.isEmpty()) {
            nodeLookup.remove(min.value)
        }
        return min
    }

    private fun consolidate() {
        // #MagicNumbers
        val degreeArray = Array<Node<E>?>(ceil(ln(size.toDouble()) * 1.5).toInt()) { null }
        val newRoots = mutableListOf<Node<E>>()
        for (node in roots) {
            insertRoot(degreeArray, node)
        }

        for (node in degreeArray) {
            if (node != null) {
                newRoots.add(node)
            }
        }
        roots.clear()
        roots.addAll(newRoots)
        min = roots.minBy { it.value }
    }

    private fun insertRoot(arrayOfRoots: Array<Node<E>?>, node: Node<E>) {
        val degree = node.children.size
        if (arrayOfRoots[degree] == null) {
            arrayOfRoots[degree] = node
            return
        }
        val other = arrayOfRoots[degree]!!
        if (other.value > node.value) {
            other.parent = node
            node.children.add(other)
            arrayOfRoots[degree] = null
            insertRoot(arrayOfRoots, node)
        } else {
            node.parent = other
            other.children.add(node)
            arrayOfRoots[degree] = null
            insertRoot(arrayOfRoots, other)
        }
    }

    private fun delete(node: Node<E>): Node<E>? {
        nodeLookup[node.value]!!.remove(node)
        if (nodeLookup[node.value]!!.isEmpty()) {
            nodeLookup.remove(node.value)
        }
        decreaseKey(node, min!!.value)
        return deleteMin()
    }

    private fun findMin(): Node<E>? {
        return min
    }

    override fun add(element: E): Boolean {
        insert(element)
        return true
    }

    override fun addAll(elements: Collection<E>): Boolean {
        elements.forEach { insert(it) }
        return true
    }

    override fun clear() {
        roots.clear()
        min = null
        size = 0
    }

    private fun getListOfChildren(node: Node<E>): MutableList<Node<E>> {
        val list = mutableListOf<Node<E>>()
        list.add(node)
        node.children.forEach { list.addAll(getListOfChildren(it)) }
        return list
    }

    override fun iterator(): MutableIterator<E> {

        val list = mutableListOf<E>()
        for (node in roots) {
            list.addAll(getListOfChildren(node).map { it.value })
        }

        /*
        val list = nodeLookup.values
            .reduce{ acc, list -> acc.addAll(list); acc }
            .map { it.value }
            .toMutableList()
         */
        return list.iterator()
    }

    override fun remove(): E {
        if (isEmpty())
            throw NoSuchElementException()
        return deleteMin()!!.value
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        var deleted = false
        for (node in nodeLookup.keys) {
            if (elements.contains(node)) continue

            val deleteList = nodeLookup[node]!!.map { it }
            deleteList.forEach { delete(it) }
            deleted = true
        }

        for (element in elements.toSet()) {
            if (!nodeLookup.containsKey(element)) continue

            val occurrences = elements.count { it == element }
            if (nodeLookup[element]!!.size > occurrences) {
                val list = nodeLookup[element]!!
                for (i in 0 until list.size - occurrences)
                    delete(list[i])

                deleted = true
            }
        }

        return deleted
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        var deleted = false
        for (element in elements) {
            if (nodeLookup.containsKey(element)) {
                delete(nodeLookup[element]!!.last())
                deleted = true
            }
        }
        return deleted
    }

    override fun remove(element: E): Boolean {
        if (nodeLookup.containsKey(element)) {
            delete(nodeLookup[element]!!.last())
            return true
        }
        return false
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        for (element in elements) {
            if (!nodeLookup.containsKey(element)) {
                return false
            }
        }
        return true
    }

    override fun contains(element: E): Boolean {
        return nodeLookup.containsKey(element)
    }

    override fun isEmpty(): Boolean {
        return roots.isEmpty()
    }

    override fun poll(): E? {
        return deleteMin()?.value
    }

    override fun element(): E {
        if (min == null) {
            throw NoSuchElementException("Empty heap")
        }
        return min!!.value
    }

    override fun peek(): E? {
        return findMin()?.value
    }

    override fun offer(e: E): Boolean {
        insert(e)
        return true
    }
}
