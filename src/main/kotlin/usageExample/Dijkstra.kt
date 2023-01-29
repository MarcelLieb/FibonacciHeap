package usageExample

import fibonacciHeap.FibonacciHeap
import java.util.*
import kotlin.system.measureTimeMillis

data class Node (val id: String)

data class Distance(val from: Node, val to: Node, val weight: Int): Comparable<Distance> {
    override fun compareTo(other: Distance): Int {
        return weight.compareTo(other.weight)
    }
}

class Graph<E>(nodes: Collection<E>, edges: Collection<Triple<E, E, Int>>) {
    private val _nodes = mutableListOf<E>()

    val nodes: List<E>
        get() = _nodes.toList()
    private val edges = mutableMapOf<E, List<Pair<E, Int>>>()

    init {
        this._nodes.addAll(nodes)
        edges.forEach { (from, to, weight) ->
            addEdge(from, to, weight)
        }
    }
    fun addNode(node: E) {
        _nodes.add(node)
    }
    fun addEdge(from: E, to: E, weight: Int) {
        if (edges.containsKey(from)) {
            edges[from] = edges[from]!!.plus(Pair(to, weight))
        } else {
            edges[from] = listOf(Pair(to, weight))
        }
    }

    fun getNeighbours(node: E): List<Pair<E, Int>> {
        return edges[node] ?: emptyList()
    }

    fun forallEdges(action: (E, E, Int) -> Unit) {
        edges.forEach { (from, tos) ->
            tos.forEach { (to, weight) ->
                action(from, to, weight)
            }
        }
    }

    fun forallAdjEdges(node: E, action: (E, E, Int) -> Unit) {
        if (edges[node] == null) return
        edges[node]!!.forEach { (to, weight) ->
            action(node, to, weight)
        }
    }

    fun forallNodes(action: (E) -> Unit) {
        _nodes.forEach { action(it) }
    }
}

class Dijkstra {
    fun shortestPathFibonacci(graph: Graph<Node>, start: Node): Map<Node, Int> {
        val distances = mutableMapOf<Node, Int>()
        val queue: FibonacciHeap<Distance> = FibonacciHeap()
        val infinity = Int.MAX_VALUE

        graph.forallNodes { node ->
            distances[node] = if (node == start) 0 else infinity
        }
        graph.forallNodes { node ->
            queue.add(Distance(start, node, distances[node]!!))
        }
        while (queue.isNotEmpty()) {
            val edge = queue.poll()!!
            val (_, node, _) = edge
            graph.forallAdjEdges(node) { from, to, weight ->
                val newDistance = distances[from]!! + weight
                if (newDistance < distances[to]!!) {
                    val success = queue.decreaseKey(Distance(start, to, distances[to]!!), Distance(start, to, newDistance))
                    if (!success) {
                        println("Error")
                    }
                    distances[to] = newDistance
                }
            }
        }
        return distances
    }

    fun shortestPath(
        graph: Graph<Node>,
        start: Node,
        queue: PriorityQueue<Distance>
    ): MutableMap<Node, Int> {
        val distances = mutableMapOf<Node, Int>()
        val infinity = Int.MAX_VALUE

        graph.forallNodes { node ->
            distances[node] = if (node == start) 0 else infinity
        }
        graph.forallNodes { node ->
            queue.add(Distance(start, node, distances[node]!!))
        }
        while (queue.isNotEmpty()) {
            val edge = queue.poll()!!
            val (_, node, _) = edge
            graph.forallAdjEdges(node) { from, to, weight ->
                val newDistance = distances[from]!! + weight
                if (newDistance < distances[to]!!) {
                    queue.remove(Distance(start, to, distances[to]!!))
                    queue.add(Distance(start, to, newDistance))
                    distances[to] = newDistance
                }
            }
        }
        return distances
    }
}

data class benchmarkConfig(val nodeCount: Int, val edgesPerNode: Int)

fun benchmarkQueues(nodeCount: Int, edgesPerNode: Int) {
    println("Number of Nodes: $nodeCount")
    println("Number of Edges: ${nodeCount * edgesPerNode}")
    val nodes = List(nodeCount) { Node(it.toString()) }
    val edges = mutableListOf<Triple<Node, Node, Int>>()

    for (i in 0 until nodeCount) {
        for (j in 0 until edgesPerNode) {
            edges.add(Triple(nodes[i], nodes[(i + j) % nodeCount], (1..20).random()))
        }
    }
    val graph = Graph(nodes, edges)
    val dijkstra = Dijkstra()

    println()

    val fib: Map<Node, Int>
    val fibdk: Map<Node, Int>
    val pri: Map<Node, Int>

    val priorityQueue = PriorityQueue<Distance>()
    val fibonacciHeap = FibonacciHeap<Distance>()

    val start = Node("0")

    val fd = measureTimeMillis { fibdk = dijkstra.shortestPathFibonacci(graph, start) }
    println("Fibonacci Heap with decrease Key: $fd ms")
    val f = measureTimeMillis { fib = dijkstra.shortestPath(graph, start, fibonacciHeap) }
    println("Fibonacci Heap with remove then add: $f ms")
    val s = measureTimeMillis { pri = dijkstra.shortestPath(graph, start, priorityQueue) }
    println("Java PriorityQueue with remove then add: $s ms")
    println()

    if (fib != fibdk || fib != pri) println("Something went wrong")
}

fun benchmarkQueues(configs: List<benchmarkConfig>) {
    for (config in configs) {
        val (nodeCount, nodesPerEdge) = config
        benchmarkQueues(nodeCount, nodesPerEdge)
    }
}

fun main() {
    val benchmark = listOf(
        benchmarkConfig(100_000, 100),
        benchmarkConfig(100_000, 1_000),
        benchmarkConfig(500_000, 2)
    )
    benchmarkQueues(benchmark)
}