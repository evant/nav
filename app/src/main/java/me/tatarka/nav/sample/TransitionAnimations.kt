@file:OptIn(ExperimentalAnimationApi::class)

package me.tatarka.nav.sample

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import me.tatarka.nav.NavigatorScope

@Composable
fun NavigatorScope<Page>.AnimateTransition(
    page: Page,
    content: @Composable () -> Unit
) {
    val current = pageTransition.currentState
    val next = pageTransition.targetState

    val (exitTransition, enterTransition) = when {
        current is Page.Home && next is Page.Settings -> slideAndFadeLeft()
        current is Page.Settings && next is Page.Home -> slideAndFadeRight()
        current is Page.Home && next is Page.Search -> slideAndFadeRight()
        current is Page.Search && next is Page.Home -> slideAndFadeLeft()
        current is Page.Search && next is Page.SearchResults -> slideUpOver()
        current is Page.SearchResults && next is Page.Search -> slideDownAndOut()
        // default to cross-fade
        else -> fadeOut() to fadeIn()
    }

    pageTransition.AnimatedVisibility(
        visible = { page == it },
        enter = enterTransition,
        exit = exitTransition,
    ) {
        content()
    }
}

fun slideAndFadeLeft() =
    fadeOut() + slideOutHorizontally() to fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 })

fun slideAndFadeRight() =
    fadeOut() + slideOutHorizontally(targetOffsetX = { it / 2 }) to fadeIn() + slideInHorizontally()

fun slideUpOver() =
    fadeOut() to slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()

fun slideDownAndOut() =
    slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut() to fadeIn()