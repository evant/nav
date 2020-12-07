package me.tatarka.nav

import assertk.assertThat
import assertk.assertions.containsExactly
import org.junit.Test

class BackStackTest {

    @Test
    fun `popTo non inclusive pops to the given page before adding`() {
        val backStack = BackStack(mutableListOf("one", "two"))
        backStack.navigate("three", popUpTo = { it == "one" })

        assertThat(backStack.pages).containsExactly("one", "three")
    }

    @Test
    fun `popTo inclusive pops the given page before adding`() {
        val backStack = BackStack(mutableListOf("one", "two"))
        backStack.navigate("three", popUpTo = { it == "two" }, inclusive = true)

        assertThat(backStack.pages).containsExactly("one", "three")
    }

    @Test
    fun `singleTop adds page if it is not already there`() {
        val backStack = BackStack(mutableListOf("one"))
        backStack.navigate("two", singleTop = true)

        assertThat(backStack.pages).containsExactly("one", "two")
    }

    @Test
    fun `singleTop skips adding page if it is already there`() {
        val backStack = BackStack(mutableListOf("one"))
        backStack.navigate("one", singleTop = true)

        assertThat(backStack.pages).containsExactly("one")
    }

}