package me.tatarka.nav

import androidx.compose.runtime.snapshots.SnapshotStateList
import assertk.Assert
import assertk.assertThat
import assertk.assertions.containsExactly
import org.junit.Test

class BackStackTest {

    @Test
    fun `popTo non inclusive pops to the given page before adding`() {
        val backStack = BackStack(snapshotListOf("one", "two"))
        backStack.navigate("three") {
            popUpTo { it == "one" }
        }

        assertThat(backStack.pages).containsExactly("one", "three")
    }

    @Test
    fun `popTo inclusive pops the given page before adding`() {
        val backStack = BackStack(snapshotListOf("one", "two"))
        backStack.navigate("three") {
            popUpTo(inclusive = true) { it == "two" }
        }

        assertThat(backStack.pages).containsExactly("one", "three")
    }

    @Test
    fun `singleTop adds page if it is not already there`() {
        val backStack = BackStack(snapshotListOf("one"))
        backStack.navigate("two") { singleTop = true }

        assertThat(backStack.pages).containsExactly("one", "two")
    }

    @Test
    fun `singleTop skips adding page if it is already there`() {
        val backStack = BackStack(snapshotListOf("one"))
        backStack.navigate("one") { singleTop = true }

        assertThat(backStack.pages).containsExactly("one")
    }

    @Test
    fun `restores popped state navigating non inclusive`() {
        val backStack = BackStack(snapshotListOf("one", "two"))
        backStack.navigate("three") { popUpTo(saveState = true) { it == "one" } }
        backStack.navigate("one") {
            popUpTo(saveState = true) { it == backStack.root }
            singleTop = true
            restoreState = true
        }

        assertThat(backStack.pages).containsExactly("one", "two")
    }

    @Test
    fun `restores popped state navigating non inclusive 2`() {
        val backStack = BackStack(snapshotListOf("one"))
        backStack.navigate("two") {
            popUpTo(saveState = true) { it == backStack.root }
            singleTop = true
            restoreState = true
        }
        backStack.navigate("three")
        backStack.navigate("one") {
            popUpTo(saveState = true) { it == backStack.root }
            singleTop = true
            restoreState = true
        }
        backStack.navigate("two") {
            popUpTo(saveState = true) { it == backStack.root }
            singleTop = true
            restoreState = true
        }

        assertThat(backStack.pages).containsExactly("one", "two", "three")
    }

    @Test
    fun `pop restores state`() {
        val backStack = BackStack(snapshotListOf("one", "two"))
        backStack.navigate("three") {
            popUpTo(saveState = true) { it == backStack.root }
            singleTop = true
            restoreState = true
        }
        backStack.pop()

        assertThat(backStack.pages).containsExactly("one", "two")
    }

    @Test
    fun `pop doesnt restore wrong state`() {
        val backStack = BackStack(snapshotListOf("one"))
        backStack.navigate("two")
        backStack.navigate("three") {
            popUpTo(saveState = true) { it == backStack.root }
            singleTop = true
            restoreState = true
        }
        backStack.navigate("four")
        backStack.navigate("one") {
            popUpTo(saveState = true) { it == backStack.root }
            singleTop = true
            restoreState = true
        }
        backStack.pop()

        assertThat(backStack.pages).containsExactly("one")
    }

    private fun <T> snapshotListOf(vararg elements: T) = SnapshotStateList<T>().apply {
        addAll(elements)
    }
}