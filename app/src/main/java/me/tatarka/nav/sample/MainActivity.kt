package me.tatarka.nav.sample

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.saveable.rememberSaveable
import me.tatarka.nav.backStackOf
import me.tatarka.nav.router.DeepLinkHandler
import me.tatarka.nav.router.parseRoute

class MainActivity : ComponentActivity() {

    private val deepLinkHandler = DeepLinkHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var primary by rememberSaveable { mutableStateOf(backStackOf<Page>(Page.Home)) }
            var search by rememberSaveable { mutableStateOf(backStackOf<SearchPage>(SearchPage.Main)) }
            var home by rememberSaveable { mutableStateOf(backStackOf<HomePage>(HomePage.List)) }

            deepLinkHandler.OnDeepLink { link ->
                primary = backStackOf(parseRoute(link))
                search = backStackOf(parseRoute(link))
                home = backStackOf(parseRoute(link))
            }

            App(AppBackStack(primary, search, home))
        }
    }

    override fun onNewIntent(intent: Intent?) {
        deepLinkHandler.onNewIntent(intent)
    }
}