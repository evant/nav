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
            var backStack by rememberSaveable { mutableStateOf(backStackOf<Page>(Page.Home)) }

            deepLinkHandler.OnDeepLink { link ->
                backStack = backStackOf(parseRoute(link))
            }

            App(backStack)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        deepLinkHandler.onNewIntent(intent)
    }
}