package me.tatarka.nav.router

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onSubscription

private const val KEY_STATE_RESTORED = "me.tatarka.nav.STATE_RESTORED"

/**
 * Helper for delivering deep link intents from your Activity to your compose tree.
 */
class DeepLinkHandler(private val activity: ComponentActivity) {

    private val deepLinkFlow = MutableSharedFlow<Uri>()

    private lateinit var deepLink: Flow<Uri>

    init {
        activity.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_CREATE) {
                    val savedStateRegistry = activity.savedStateRegistry
                    initDeepLink(savedStateRegistry.consumeRestoredStateForKey(KEY_STATE_RESTORED) == null)
                    savedStateRegistry.registerSavedStateProvider(KEY_STATE_RESTORED) { Bundle() }
                }
            }
        })
    }

    private fun initDeepLink(isInitial: Boolean) {
        deepLink = if (isInitial) {
            deepLinkFlow.onSubscription {
                val data = activity.intent?.data
                if (data != null) {
                    emit(data)
                }
            }
        } else {
            deepLinkFlow
        }
    }

    /**
     * Call in [android.app.Activity.onNewIntent]
     */
    fun onNewIntent(intent: Intent?) {
        activity.intent = intent
        activity.lifecycleScope.launchWhenCreated {
            val data = intent?.data
            if (data != null) {
                deepLinkFlow.emit(data)
            }
        }
    }


    /**
     * Triggers the callback whenever a new deep link is delivered.
     */
    @Composable
    fun OnDeepLink(callback: suspend (Uri?) -> Unit) {
        LaunchedEffect(null) {
            deepLink.collect(callback)
        }
    }
}
