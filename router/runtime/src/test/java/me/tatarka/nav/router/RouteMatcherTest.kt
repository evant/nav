package me.tatarka.nav.router

import android.net.Uri
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RouteMatcherTest {
    @Test
    fun matches_empty_path() {
        val routeMatcher = RouteMatcher("")

        assertThat(routeMatcher.match(Uri.parse("https://example.com")))
            .isNotNull()
    }

    @Test
    fun does_not_match_empty_path() {
        val routeMatcher = RouteMatcher("")

        assertThat(routeMatcher.match(Uri.parse("https://example.com/detail")))
            .isNull()
    }

    @Test
    fun matches_simple_path() {
        val routeMatcher = RouteMatcher("/detail")

        assertThat(routeMatcher.match(Uri.parse("https://example.com/detail")))
            .isNotNull()
    }

    @Test
    fun does_not_match_simple_path() {
        val routeMatcher = RouteMatcher("/detail")

        assertThat(routeMatcher.match(Uri.parse("https://example.com/other")))
            .isNull()
    }

    @Test
    fun matches_with_param() {
        val routeMatcher = RouteMatcher("/detail/{id}")

        assertThat(routeMatcher.match(Uri.parse("https://example.com/detail/1")))
            .isNotNull().containsOnly("id" to "1")
    }

    @Test
    fun matches_query_param_present() {
        val routeMatcher = RouteMatcher("/search?query={query}")

        assertThat(routeMatcher.match(Uri.parse("https://example.com/search?query=test")))
            .isNotNull().containsOnly("query" to "test")
    }

    @Test
    fun matches_query_param_missing() {
        val routeMatcher = RouteMatcher("/search?query={query}")

        assertThat(routeMatcher.match(Uri.parse("https://example.com/search")))
            .isNotNull().isEmpty()
    }
}
