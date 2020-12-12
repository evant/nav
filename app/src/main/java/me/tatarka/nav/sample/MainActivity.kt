package me.tatarka.nav.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.setContent
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private lateinit var deepLink: MutableStateFlow<Uri?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLink = MutableStateFlow(intent?.data)
        setContent { App(deepLink = deepLink.collectAsState().value) }
    }

    override fun onNewIntent(intent: Intent?) {
        setIntent(intent)
        deepLink.value = intent?.data
    }
}