package me.tatarka.nav.sample

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.tatarka.nav.router.Route

sealed class Page : Parcelable {
    @Parcelize
    @Route("/search")
    object Search : Page()

    @Parcelize
    @Route("")
    @Route("/detail/.*")
    object Home : Page()

    @Parcelize
    @Route("/settings")
    object Settings : Page()
}

sealed class HomePage : Parcelable {
    @Parcelize
    @Route("")
    object List : HomePage()

    @Parcelize
    @Route("/detail/{id}")
    data class Detail(val id: Int) : HomePage()
}

sealed class SearchPage : Parcelable {
    @Parcelize
    @Route("/search")
    object Main : SearchPage()

    @Parcelize
    @Route("/search/{query}")
    data class Results(val query: String) : SearchPage()
}

