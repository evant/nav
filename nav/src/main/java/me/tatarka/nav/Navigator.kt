package me.tatarka.nav

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.ExperimentalRestorableStateHolder
import androidx.compose.runtime.savedinstancestate.rememberRestorableStateHolder
import androidx.compose.ui.platform.AmbientContext

@Composable
@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> Navigator(
    stack: NavigationStack<T>,
    noinline content: @Composable () -> Unit
) {
    Navigator(pages = stack.pages, onPopPage = { stack.pop() }, content = content)
}

/**
 * Navigator is a component that manages a stack of pages in your application. The top page of the
 * stack will be shown, and the instance state of the other pages will be remembered.
 *
 * As you pass in a list of pages, you have complete control over your back stack. If you want a
 * more opinionated approach, see [BackStack].
 *
 * @param pages The pages in the stack, top one will be shown. This must not be empty.
 * @param onPopPage Called when the user press the back button and requests a page to be popped off
 * the stack.
 * @param content The page content. This will normally depend on the top page in the stack.
 */
@OptIn(ExperimentalRestorableStateHolder::class)
@Composable
fun <T : Any> Navigator(
    pages: List<T>,
    onPopPage: (page: T) -> Unit,
    content: @Composable () -> Unit
) {
    require(pages.isNotEmpty()) { "pages must not be empty" }
    val pages = pages.toList() // ensure the list is immutable

    var backstack by remember { mutableStateOf(pages) }
    val restorableStateHolder = rememberRestorableStateHolder<T>()

    val currentPage = pages.last()

    onCommit(pages) {
        if (pages != backstack) {
            val oldPages = backstack
            backstack = pages
            for (oldPage in oldPages) {
                if (oldPage !in pages) {
                    restorableStateHolder.removeState(oldPage)
                }
            }
        }
    }

    OnBackPressed(currentPage, enabled = pages.size > 1) {
        onPopPage(currentPage)
    }

    restorableStateHolder.RestorableStateProvider(key = currentPage) {
        content()
    }
}

/**
 * Fires the given callback when the back button is pressed.
 */
@Composable
private fun OnBackPressed(vararg inputs: Any?, enabled: Boolean = true, onBackPressed: () -> Unit) {
    val onBackPressedDispatcher = AmbientContext.current.onBackPressedDispatcher
    val callback = remember(*inputs) {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
    }
    DisposableEffect(callback) {
        onBackPressedDispatcher.addCallback(callback)
        onDispose {
            callback.remove()
        }
    }
    callback.isEnabled = enabled
}

private val Context.onBackPressedDispatcher: OnBackPressedDispatcher
    get() = when (this) {
        is OnBackPressedDispatcherOwner -> onBackPressedDispatcher
        is ContextWrapper -> baseContext.onBackPressedDispatcher
        else -> throw IllegalArgumentException("Expected context to be OnBackPressedDispatcherOwner, are you extending ComponentActivity?")
    }

interface NavigationStack<T> {
    val pages: List<T>

    fun pop(): Boolean
}