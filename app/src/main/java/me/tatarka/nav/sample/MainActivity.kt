package me.tatarka.nav.sample

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App(deepLink = intent?.data ?: Uri.EMPTY) }
    }
}