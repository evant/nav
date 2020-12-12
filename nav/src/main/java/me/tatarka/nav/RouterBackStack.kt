package me.tatarka.nav

import android.net.Uri
import me.tatarka.nav.router.toRoute

inline fun <reified T : Any> Uri?.toBackStack(startPage: T, vararg pages: T): BackStack<T> {
    val deepLinkPage = this?.toRoute<T>()
    return if (deepLinkPage == null || startPage == deepLinkPage) {
        backStackOf(startPage, *pages)
    } else {
        backStackOf(startPage, *pages, deepLinkPage)
    }
}