package me.tatarka.nav

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun <T : Any> Navigator(
    stack: NavigationStack<T>,
    modifier: Modifier = Modifier,
    content: @Composable NavigatorScope<T>.(page: T) -> Unit
) {
    val state = rememberNavigatorState(stack.pages)
    state.pages = stack.pages
    Navigator(
        state = state,
        onPopPage = { stack.pop() },
        modifier = modifier,
        content = content
    )
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
 * @param content The page content. Use the provided page to determine which page to show. During a
 * transition this will be called twice with the initial and target pages to animate them.
 */
@Composable
fun <T : Any> Navigator(
    pages: List<T>,
    onPopPage: (page: T) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable NavigatorScope<T>.(page: T) -> Unit
) {
    val state = rememberNavigatorState(pages)
    state.pages = pages
    Navigator(state, onPopPage, modifier, content)
}

/**
 * Navigator is a component that manages a stack of pages in your application. The top page of the
 * stack will be shown, and the instance state of the other pages will be remembered.
 *
 * As you pass in a list of pages, you have complete control over your back stack. If you want a
 * more opinionated approach, see [BackStack].
 *
 * @param state The navigator state, see [rememberNavigatorState].
 * @param onPopPage Called when the user press the back button and requests a page to be popped off
 * the stack.
 * @param content The page content. Use the provided page to determine which page to show. During a
 * transition this will be called twice with the initial and target pages to animate them.
 */
@Composable
fun <T : Any> Navigator(
    state: NavigatorState<T>,
    onPopPage: (page: T) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable NavigatorScope<T>.(page: T) -> Unit
) {
    val transition = updateTransition(state.pages.last(), label = "navigation")
    val shownPages = calculatePagesToShow(transition, state.transitionOrdering)

    OnBackPressed(transition.targetState, enabled = state.pages.size > 1) {
        onPopPage(transition.targetState)
    }

    Box(modifier) {
        for (page in shownPages) {
            key(page) {
                val scope = NavigatorScopeImpl(transition)
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

class NavigatorState<T : Any>(
    initialPages: List<T>,
    internal val saveableStateHolder: SaveableStateHolder,
    private val shouldSaveState: (page: T) -> Boolean = { false },
    private val ordering: ((previousPages: List<T>) -> TransitionOrdering)? = null,
) {
    init {
        require(initialPages.isNotEmpty()) { "pages must not be empty" }
    }

    private var _pages by mutableStateOf(initialPages.toList())

    var transitionOrdering: TransitionOrdering = TransitionOrdering.TARGET_ON_TOP
        private set

    var pages: List<T> = _pages
        set(value) {
            require(value.isNotEmpty()) { "pages must not be empty" }
            if (field == value) {
                transitionOrdering = TransitionOrdering.TARGET_ON_TOP
                return
            }
            for (previousPage in field) {
                if (previousPage !in value && !shouldSaveState(previousPage)) {
                    saveableStateHolder.removeState(previousPage)
                }
            }
            transitionOrdering = ordering?.invoke(field) ?: defaultOrdering(field, value)
            field = value.toList()
        }
}

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

/**
 * Creates a [NavigatorState] and remembers it.
 *
 * @param initialPages The initial pages in the stack, top one will be shown. This must not be empty.
 * @param ordering Determine which page is on top when showing the transition animation. This will
 * provide the list of previous pages to calculate this. The default implementation will show the
 * target page on top if it's not in the previous pages (push) and show the initial page on top
 * otherwise (pop).
 * @param shouldSaveState Determine if old pages no longer in [initialPages] should keep their saved
 * state. This can be used 'swap' back stacks saving their state. By default this is always returns
 * false.
 */
@Composable
fun <T : Any> rememberNavigatorState(
    initialPages: List<T>,
    shouldSaveState: (page: T) -> Boolean = { false },
    ordering: ((previousPages: List<T>) -> TransitionOrdering)? = null,
): NavigatorState<T> {
    val saveableStateHolder = rememberSaveableStateHolder()
    return remember {
        NavigatorState(
            initialPages = initialPages,
            saveableStateHolder = saveableStateHolder,
            shouldSaveState = shouldSaveState,
            ordering = ordering,
        )
    }
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

    fun shouldSaveState(page: T): Boolean
}