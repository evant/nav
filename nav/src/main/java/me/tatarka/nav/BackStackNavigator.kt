package me.tatarka.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.savedinstancestate.Saver
import androidx.compose.runtime.savedinstancestate.autoSaver
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * [remember] a [BackStack], saving it's pages instance state. If your pages are types that can be
 * stored inside a Bundle then it will be saved and restored automatically using [autoSaver],
 * otherwise you will need to provide a custom [Saver] implementation via the [saver] param.
 *
 * @param saver The [Saver] object which defines how pages are saved and restored.
 * @param backStack The [BackStack] to save.
 */
@Composable
fun <T : Any> rememberBackStack(
    vararg inputs: Any?,
    saver: Saver<T, Any> = autoSaver(),
    backStack: () -> BackStack<T>
): BackStack<T> {
    return savedInstanceState(inputs, saver = backStackSaver(saver)) { backStack() }.value
}

fun <T : Any> backStackOf(startingPage: T): BackStack<T> =
    BackStack(mutableStateListOf(startingPage))

fun <T : Any> backStackOf(startingPage: T, deepLink: T? = null): BackStack<T> =
    if (deepLink != startingPage) {
        BackStack(mutableStateListOf(startingPage))
    } else {
        BackStack(mutableStateListOf(startingPage, deepLink))
    }

private fun <T : Any> backStackSaver(saver: Saver<T, Any>): Saver<BackStack<T>, List<Any>> =
    Saver(
        save = { backStack -> backStack.pages.map { with(saver) { save(it)!! } } },
        restore = { list ->
            BackStack(SnapshotStateList<T>().apply {
                addAll(list.map { saver.restore(it)!! })
            })
        }
    )

/**
 * Navigator is a component that manages a stack of pages in your application. The current page of
 * the back stack will be shown, and the instance state of the other pages will be remembered.
 *
 * @param backStack The back stack, the top one will be shown.
 * @param content The page content. This will normally depend on the top page in the stack.
 */
@Composable
fun <T : Any> Navigator(
    backStack: BackStack<T>,
    content: @Composable () -> Unit
) {
    Navigator(pages = backStack.pages, onPopPage = { backStack.pop() }) {
        content()
    }
}