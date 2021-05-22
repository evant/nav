package me.tatarka.nav

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

@Composable
@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> Navigator(
    stack: NavigationStack<T>,
    noinline content: @Composable NavigatorScope<T>.(page: T) -> Unit
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
 * @param ordering Determine which page is on top when showing the transition animation. This will
 * provide the list of previous pages to calculate this. The default implementation will show the
 * target page on top if it's not in the previous pages (push) and show the initial page on top
 * otherwise (pop).
 * @param content The page content. Use the provided page to determine which page to show. During a
 * transition this will be called twice with the initial and target pages to animate them.
 */
@Composable
fun <T : Any> Navigator(
    pages: List<T>,
    onPopPage: (page: T) -> Unit,
    ordering: (previousPages: List<T>) -> TransitionOrdering = { defaultOrdering(it, pages) },
    content: @Composable NavigatorScope<T>.(page: T) -> Unit
) {
    val state = rememberNavigatorState(pages = pages, calculateOrdering = ordering)
    val shownPages = calculatePagesToShow(state.transition, state.transitionOrdering)

    OnBackPressed(state.transition.targetState, enabled = state.onBackPressedEnabled) {
        onPopPage(state.transition.targetState)
    }

    Box {
        for (page in shownPages) {
            key(page) {
                val scope = NavigatorScopeImpl(state.transition)
                state.saveableStateHolder.SaveableStateProvider(key = page) {
                    scope.content(page)
                }
            }
        }
    }
}

private fun <T> defaultOrdering(previousPages: List<T>, currentPages: List<T>): TransitionOrdering {
    return if (currentPages.last() in previousPages) {
        TransitionOrdering.INITIAL_ON_TOP
    } else {
        TransitionOrdering.TARGET_ON_TOP
    }
}

private fun <T : Any> calculatePagesToShow(
    transition: Transition<T>,
    ordering: TransitionOrdering
): List<T> {
    val renderPages = mutableListOf<T>()
    if (transition.currentState != transition.targetState) {
        renderPages.add(transition.currentState)
    }
    renderPages.add(transition.targetState)
    if (ordering == TransitionOrdering.INITIAL_ON_TOP) {
        renderPages.reverse()
    }
    return renderPages
}

/**
 * Scope for children of [Navigator].
 */
@Stable
interface NavigatorScope<T : Any> {
    /**
     * A transition from the previous to current page which can be used for animations. Typically
     * you'd animate the visibility of the pages using [Transition.AnimatedVisibility].
     *
     * ```
     * Navigator(backStack) { page ->
     *   pageTransition.AnimatedVisibility(
     *     visible = { page == it },
     *     enter = fadeIn(),
     *     exit = fadeOut(),
     *   ) {
     *     ...
     *   }
     * }
     * ```
     */
    val pageTransition: Transition<T>
}

private data class NavigatorScopeImpl<T : Any>(
    override val pageTransition: Transition<T>
) : NavigatorScope<T>

private data class NavigatorState<T : Any>(
    val saveableStateHolder: SaveableStateHolder,
    val transition: Transition<T>,
    val onBackPressedEnabled: Boolean,
    val transitionOrdering: TransitionOrdering,
)

/**
 * Defines the render ordering of the pages during a transition.
 */
enum class TransitionOrdering {
    /**
     * The initial page is on top.
     */
    INITIAL_ON_TOP,

    /**
     * The target page is on top.
     */
    TARGET_ON_TOP
}

@Composable
private fun <T : Any> rememberNavigatorState(
    pages: List<T>,
    calculateOrdering: (previousPages: List<T>) -> TransitionOrdering
): NavigatorState<T> {
    require(pages.isNotEmpty()) { "pages must not be empty" }
    val pages = pages.toList() // ensure the list is immutable

    var backstack by remember { mutableStateOf(pages) }
    val saveableStateHolder = rememberSaveableStateHolder()

    val currentPage = pages.last()

    val ordering = if (pages != backstack) {
        val previousPages = backstack
        backstack = pages
        for (oldPage in previousPages) {
            if (oldPage !in pages) {
                saveableStateHolder.removeState(oldPage)
            }
        }
        calculateOrdering(previousPages)
    } else {
        TransitionOrdering.TARGET_ON_TOP
    }

    val transition = updateTransition(targetState = currentPage, label = "navigation")

    return NavigatorState(
        saveableStateHolder = saveableStateHolder,
        transition = transition,
        onBackPressedEnabled = pages.size > 1,
        transitionOrdering = ordering,
    )
}

/**
 * Fires the given callback when the back button is pressed.
 */
@Composable
private fun OnBackPressed(vararg inputs: Any?, enabled: Boolean = true, onBackPressed: () -> Unit) {
    val onBackPressedDispatcher = LocalContext.current.onBackPressedDispatcher
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