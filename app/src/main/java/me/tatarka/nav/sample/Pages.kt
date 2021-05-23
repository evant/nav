package me.tatarka.nav.sample

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.tatarka.nav.router.Route

sealed class Page : Parcelable {
    @Parcelize
    @Route("/search")
    object Search : Page()

    @Parcelize
    @Route("/search/{query}")
    data class SearchResults(val query: String) : Page()

    @Parcelize
    @Route("", root = true)
    object Home : Page()

    @Parcelize
    @Route("/detail/{id}")
    data class Detail(val id: Int) : Page()

    @Parcelize
    @Route("/settings")
    object Settings : Page()
}