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
        backStack.navigate("three", popUpTo = { it == "one" })

        assertThat(backStack.pages).containsExactly2("one", "three")
    }

    @Test
    fun `popTo inclusive pops the given page before adding`() {
        val backStack = BackStack(snapshotListOf("one", "two"))
        backStack.navigate("three", popUpTo = { it == "two" }, inclusive = true)

        assertThat(backStack.pages).containsExactly2("one", "three")
    }

    @Test
    fun `singleTop adds page if it is not already there`() {
        val backStack = BackStack(snapshotListOf("one"))
        backStack.navigate("two", singleTop = true)

        assertThat(backStack.pages).containsExactly2("one", "two")
    }

    @Test
    fun `singleTop skips adding page if it is already there`() {
        val backStack = BackStack(snapshotListOf("one"))
        backStack.navigate("one", singleTop = true)

        assertThat(backStack.pages).containsExactly2("one")
    }

    private fun <T> snapshotListOf(vararg elements: T) = SnapshotStateList<T>().apply {
        addAll(elements)
    }

    // https://github.com/willowtreeapps/assertk/issues/332
    private fun Assert<List<*>>.containsExactly2(vararg elements: Any?) {
        transform { it.toList() }.containsExactly(*elements)
    }
}