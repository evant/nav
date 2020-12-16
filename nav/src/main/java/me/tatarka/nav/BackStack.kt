package me.tatarka.nav

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Creates a [BackStack] with the given starting page.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> backStackOf(startingPage: T): BackStack<T> =
    BackStack(mutableStateListOf(startingPage))

/**
 * Creates a [BackStack] with the given pages.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> backStackOf(startingPage: T, vararg pages: T): BackStack<T> =
    BackStack(SnapshotStateList<T>().apply {
        add(startingPage)
        addAll(pages)
    })

/**
 * Creates a [BackStack] with the given pages.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> backStackOf(pages: List<T>): BackStack<T> {
    require(pages.isNotEmpty()) { "pages must not be empty" }
    return BackStack(SnapshotStateList<T>().apply { addAll(pages) })
}

/**
 * An opinionated back stack implementation. This guarantees you always have a root page in your
 * stack and you can only push new items onto the stack and pop ones off.
 */
@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
inline class BackStack<T : Any> @PublishedApi internal constructor(
    private val _pages: SnapshotStateList<T>
) : NavigationStack<T>, Parcelable {

    /**
     * All the pages in the back stack.
     */
    override val pages: List<T> get() = _pages

    /**
     * The starting page. The is the bottom page on the stack and can never be removed.
     */
    val root: T get() = _pages.first()

    /**
     * The current page. This is at the top page on the stack and is meant to be displayed.
     */
    val current: T get() = _pages.last()

    fun set(pages: List<T>) {
        require(pages.isNotEmpty()) { "pages must not be empty" }
        require(pages.first() == root) { "cannot change root page" }
        _pages.clear()
        _pages.addAll(pages)
    }

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
    override fun pop(): Boolean {
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

    @PublishedApi
    internal constructor(parcel: Parcel) : this(
        SnapshotStateList<T>().apply {
            parcel.readList(this, this::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(_pages)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmStatic
        val CREATOR = object : Parcelable.Creator<BackStack<*>> {
            override fun createFromParcel(parcel: Parcel): BackStack<*> {
                return BackStack<Any>(parcel)
            }

            override fun newArray(size: Int): Array<BackStack<*>?> {
                return arrayOfNulls(size)
            }
        }
    }
}
