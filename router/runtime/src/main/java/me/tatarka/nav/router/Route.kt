package me.tatarka.nav.router

/**
 * Annotate variants of a sealed class with routes. This will generate a `parseRoute()` method that
 * parses a deep link path into one of the variants. You can denote parameters by using `{name}` in
 * the route to parse to the {name} field. You can use the .* wildcard to match 0 or more characters.
 * If you want to match multiple paths for a variant, you may apply the annotation multiple times.
 *
 * Note: only the path is looked at for matching, not the scheme or host. If you have more specific
 * parsing needs you should inspect those first before calling `parseRoute()`.
 *
 * ```
 * sealed class Routes {
 *     @Route("", root = true)
 *     object Home: Routes()
 *
 *     @Route("/detail/{id}")
 *     data class Detail(id: Int) : Routes()
 * }
 * ```
 *
 * ```
 * val route = Routes::class.parseRoute(Uri.parse("https://example.com/detail/1"))
 * ```
 */
@Repeatable
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Route(val value: String, val root: Boolean = false)

