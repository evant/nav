package me.tatarka.nav

/**
 * An opinionated back stack implementation. This guarantees you always have a root page in your
 * stack and you can only push new items onto the stack and pop ones off.
 */
inline class BackStack<T : Any>(private val _pages: MutableList<T>) {

    /**
     * All the pages in the back stack.
     */
    val pages: List<T> get() = _pages

    /**
     * The starting page. The is the bottom page on the stack and can never be removed.
     */
    val root: T get() = _pages.first()

    /**
     * The current page. This is at the top page on the stack and is meant to be displayed.
     */
    val current: T get() = _pages.last()

    /**
     * Navigate to the given page pushing it onto the stack.
     *
     * @return true if the back stack was changed, false otherwise.
     */
    fun navigate(page: T): Boolean {
        _pages.add(page)
        return true
    }

    /**
     * Navigate to the given page pushing it onto the stack.
     *
     * @param page The page to navigate to.
     * @param popUpTo Pop the stack up to the top-most page that returns true for the given
     * condition. Common conditions would be `{ it == MyPage }` or { it is MyPage }.
     * @param inclusive If `popUpTo` is inclusive. If true the page matching the condition will be
     * popped. Otherwise, only pages above it will be popped.
     * @param singleTop If true then the page will not be pushed on the stack if it already the top
     * page.
     *
     * @return true if the back stack was changed, false otherwise.
     */
    fun navigate(
        page: T,
        popUpTo: ((T) -> Boolean)? = null,
        inclusive: Boolean = false,
        singleTop: Boolean = false
    ): Boolean {
        var changed = false
        if (popUpTo != null) {
            val targetIndex = _pages.indexOfLast(popUpTo)
            if (targetIndex >= 0) {
                for (i in (_pages.size - 1) downTo if (inclusive) targetIndex else targetIndex + 1) {
                    changed = true
                    _pages.removeLast()
                }
            }
        }
        if (!singleTop || current != page) {
            changed = true
            _pages.add(page)
        }
        return changed
    }

    /**
     * Pops the top page off the stack if possible. Will be ignored if there only the root page is
     * on the stack.
     *
     * @return true if the back stack was changed, false otherwise.
     */
    fun pop(): Boolean {
        return if (_pages.size > 1) {
            _pages.removeLast()
            true
        } else {
            false
        }
    }

    /**
     * Pops all pages except the root off the stack.
     *
     * @return true if the back stack was changed, false otherwise.
     */
    fun popToRoot(): Boolean {
        return if (_pages.size > 1) {
            _pages.subList(1, _pages.size).clear()
            true
        } else {
            false
        }
    }

    override fun toString(): String {
        return "BackStack{${pages.joinToString(", ")}}"
    }
}
