package fibonacciHeap

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class FibonacciHeapTest {
    data class Entry(val name: String)
    data class Distance(val name: Entry, val value: Int) : Comparable<Distance> {
        override fun compareTo(other: Distance): Int {
            return value.compareTo(other.value)
        }
    }

    data class IKV<I, K: Comparable<K>, V>(val id: I, val key: K, val value: V) : Comparable<IKV<I, K, V>> {
        override fun compareTo(other: IKV<I, K, V>): Int {
            return key.compareTo(other.key)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is IKV<*, *, *>) return false
            if (id != other.id) return false
            return true
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }
    }

    @Test
    fun addRemoveCheckSize() {
        val heap = FibonacciHeap<Int>()
        assertEquals(0, heap.size)
        heap.add(1)
        assertEquals(1, heap.size)
        heap.add(2)
        assertEquals(2, heap.size)
        heap.add(3)
        assertEquals(3, heap.size)
        assertTrue(heap.remove(2))
        assertFalse(heap.contains(2))
        assertEquals(2, heap.size)
        assertEquals(1, heap.remove())
        assertEquals(1, heap.size)
        assertFalse(heap.remove(1))
        assertEquals(1, heap.size)
        heap.remove()
        assertEquals(0, heap.size)
    }

    @Test
    fun decreaseKey() {
        val heap = FibonacciHeap<Int>()
        heap.addAll(listOf(1, 2, 3, 4, 5))
        heap.decreaseKey(3, 0)
        assertEquals(0, heap.peek())
        assertEquals(5, heap.size)
    }

    @Test
    fun decreaseKeyTwice() {
        val heap = FibonacciHeap<Int>()
        heap.addAll(listOf(1, 2, 2, 4, 5))
        assertTrue(heap.decreaseKey(5, 3))
        assertEquals(1, heap.peek())
        assertTrue(heap.decreaseKey(3, 0))
        assertEquals(0, heap.peek())
        assertEquals(5, heap.size)
    }

    @Test
    fun decreaseKeyDataClass() {
        val heap = FibonacciHeap<Distance>()
        heap.add(Distance(Entry("A"), 2))
        heap.add(Distance(Entry("B"), 2))
        heap.add(Distance(Entry("C"), 3))
        heap.add(Distance(Entry("D"), 3))
        heap.add(Distance(Entry("E"), 5))
        assertTrue(heap.decreaseKey(Distance(Entry("C"), 3), Distance(Entry("C"), 1)))
        assertTrue(heap.decreaseKey(Distance(Entry("D"), 3), Distance(Entry("D"), 0)))
        assertEquals(Distance(Entry("D"), 0), heap.poll())
        assertEquals(4, heap.size)
        assertFalse(heap.decreaseKey(Distance(Entry("F"), 3), Distance(Entry("F"), 0)))
        assertFalse(heap.decreaseKey(Distance(Entry("C"), 0), Distance(Entry("C"), 3)))
    }

    @Test
    fun decreaseKeyIKV() {
        val heap = FibonacciHeap<IKV<Int, Int, Entry>>()
        heap.add(IKV(0, 2, Entry("A")))
        heap.add(IKV(1, 2, Entry("B")))
        heap.add(IKV(2, 3, Entry("C")))
        heap.add(IKV(3, 3, Entry("C")))
        heap.decreaseKey(IKV(2, 3, Entry("C")), IKV(2, 1, Entry("C")))
        assertEquals(IKV(2, 1, Entry("C")), heap.poll())
    }

    @Test
    fun decreaseKeyMassive() {
        val heap = FibonacciHeap<Int>()
        for (i in 0..1000)
            heap.add(i)
        for (i in 0..1000)
            heap.decreaseKey(i, if (1000 - i < i) 1000 - i else i)
        for (i in 0..1000)
            assertEquals(i / 2, heap.poll())
    }

    @Test
    fun addAll() {
        val heap = FibonacciHeap<Int>()
        heap.addAll(listOf(1, 2, 2, 4, 5))
        assertEquals(5, heap.size)
        assertEquals(1, heap.poll())
        assertEquals(2, heap.poll())
        assertEquals(2, heap.poll())
        assertEquals(2, heap.size)
    }

    @Test
    fun clear() {
        val heap = FibonacciHeap<Int>()
        heap.addAll(listOf(1, 2, 2, 4, 5))
        assertEquals(5, heap.size)
        heap.clear()
        assertEquals(0, heap.size)
        assertNull(heap.poll())
        assertTrue(heap.isEmpty())
        assertThrowsExactly(NoSuchElementException::class.java) { heap.remove() }
    }

    @Test
    fun iterator() {
        val heap = FibonacciHeap<Int>()
        heap.addAll(listOf(1, 2, 2, 4, 5))
        assertEquals(5, heap.size)
        assertEquals(1, heap.poll())
        heap.add(-1)
        assertEquals(-1, heap.peek())
        val list = heap.iterator().asSequence().toList()
        assertEquals(setOf(-1, 2, 2, 4, 5), list.toSet())
        assertEquals(5, list.size)
    }

    @Test
    fun remove() {
        val heap = FibonacciHeap<Int>()
        heap.addAll(listOf(1, 2, 2, 4, 5))
        assertEquals(5, heap.size)
        assertEquals(1, heap.remove())
        assertEquals(4, heap.size)
        assertEquals(2, heap.remove())
        assertEquals(3, heap.size)
        assertEquals(2, heap.remove())
        assertEquals(2, heap.size)
        assertEquals(4, heap.remove())
        assertEquals(1, heap.size)
        assertEquals(5, heap.remove())
        assertEquals(0, heap.size)
        assertThrowsExactly(NoSuchElementException::class.java) { heap.remove() }
    }

    @Test
    fun removeIKV() {
        val heap = FibonacciHeap<IKV<Int, Int, Entry>>()
        heap.add(IKV(0, 2, Entry("A")))
        heap.add(IKV(1, 2, Entry("B")))
        heap.add(IKV(2, 3, Entry("C")))
        heap.add(IKV(3, 3, Entry("C")))
        assertTrue(heap.contains(IKV(2, 3, Entry("C"))))
        assertTrue(heap.contains(IKV(2, 33, Entry("any"))))
        assertTrue(heap.remove(IKV(2, 69, Entry("anything at all"))))
        assertEquals(3, heap.size)
        assertFalse(heap.contains(IKV(2, 3, Entry("C"))))
        assertFalse(heap.remove(IKV(4, 3, Entry("C"))))
    }

    @Test
    fun retainAll() {
        val heap = FibonacciHeap<Int>()
        heap.addAll(listOf(1, 2, 2, 4, 5))
        assertTrue(heap.contains(5))
        assertEquals(5, heap.size)
        heap.retainAll(listOf(1, 1, 2, 3, 4))
        assertEquals(3, heap.size)
        assertEquals(1, heap.remove())
        assertEquals(2, heap.remove())
        assertEquals(4, heap.remove())
        assertThrowsExactly(NoSuchElementException::class.java) { heap.remove() }
    }

    @Test
    fun removeAll() {
        val heap = FibonacciHeap<Int>()
        heap.addAll(listOf(5, 2, 2, 4, 1))
        assertTrue(heap.contains(5))
        assertEquals(5, heap.size)
        heap.removeAll(listOf(1, 1, 2, 3, 4))
        assertEquals(2, heap.size)
        assertEquals(2, heap.remove())
        assertEquals(5, heap.remove())
        assertThrowsExactly(NoSuchElementException::class.java) { heap.remove() }
    }

    @Test
    fun containsAll() {
        val heap = FibonacciHeap<Int>()
        heap.addAll(listOf(1, 2, 2, 4, 5))
        assertTrue(heap.containsAll(listOf(1, 2, 4)))
        assertFalse(heap.containsAll(listOf(1, 2, 4, 6)))
    }

    @Test
    fun element() {
        val heap = FibonacciHeap<Int>()
        heap.addAll(listOf(1, 2, 2, 4, 5))
        assertEquals(1, heap.element())
        assertEquals(1, heap.element())
        assertEquals(5, heap.size)
        heap.clear()
        assertThrowsExactly(NoSuchElementException::class.java) { heap.element() }
    }

    @Test
    fun offer() {
        val heap = FibonacciHeap<Int>()
        assertTrue(heap.offer(1))
        assertTrue(heap.offer(2))
        assertTrue(heap.offer(3))
        assertTrue(heap.offer(4))
        assertTrue(heap.offer(5))
        assertEquals(5, heap.size)
        assertEquals(1, heap.remove())
        assertEquals(2, heap.remove())
        assertEquals(3, heap.remove())
        assertEquals(4, heap.remove())
        assertEquals(5, heap.remove())
        assertEquals(0, heap.size)
        assertThrowsExactly(NoSuchElementException::class.java) { heap.remove() }
    }

    @Test
    fun `Complex deleting order`() {
        val heap = FibonacciHeap<Int>()
        heap.addAll(1..100)
        heap.removeAll(listOf(1, 3, 7, 8, 15, 16, 31, 32, 63, 64, 100))
        assertEquals(89, heap.size)
        var prev: Int? = null
        var count = 0
        while (heap.isNotEmpty()) {
            val current = heap.remove()
            if (prev != null)
                assertTrue(prev < current)
            prev = current
            count++
        }
        assertEquals(89, count)
    }
}