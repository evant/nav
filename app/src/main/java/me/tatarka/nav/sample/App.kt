package me.tatarka.nav.sample

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import me.tatarka.nav.*

data class NavItem(
    @DrawableRes
    val icon: Int,
    @StringRes
    val title: Int,
    val page: Page,
)

val BOTTOM_ITEMS = listOf(
    NavItem(icon = R.drawable.ic_search, title = R.string.search, page = Page.Search),
    NavItem(icon = R.drawable.ic_home, title = R.string.home, page = Page.Home),
    NavItem(icon = R.drawable.ic_settings, title = R.string.settings, page = Page.Settings)
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun App(backStack: BackStack<Page>) {

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = when (backStack.current) {
                        Page.Search, is Page.SearchResults -> stringResource(R.string.search)
                        Page.Home, is Page.Detail -> stringResource(R.string.app_name)
                        Page.Settings -> stringResource(R.string.settings)
                    },
                    style = MaterialTheme.typography.h4
                )
            })
        },
        bottomBar = {
            BottomNavigation {
                val currentBottomPage =
                    backStack.pages.findLast { page -> BOTTOM_ITEMS.any { page == it.page } }

                for (item in BOTTOM_ITEMS) {
                    BottomNavigationItem(
                        icon = { Icon(painterResource(item.icon), contentDescription = null) },
                        label = { Text(stringResource(item.title)) },
                        selected = item.page == currentBottomPage,
                        onClick = {
                            if (item.page == backStack.current) {
                                //TODO
                            } else {
                                backStack.navigate(item.page) {
                                    popUpTo(saveState = currentBottomPage != item.page) {
                                        it == backStack.root
                                    }
                                    singleTop = true
                                    restoreState = true
                                }
                            }
                        })
                }
            }
        }) { padding ->
        Navigator(backStack, Modifier.padding(padding)) { page ->
            pageTransition.AnimatedVisibility(
                visible = { it == page },
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                when (page) {
                    is Page.Search -> {
                        Search(onSearch = { query ->
                            backStack.navigate(Page.SearchResults(query)) { singleTop = true }
                        })
                    }
                    is Page.SearchResults -> {
                        SearchResults(text = page.query)
                    }
                    is Page.Home -> {
                        Home(onItemClick = { item ->
                            backStack.navigate(Page.Detail(item))
                        })
                    }
                    is Page.Detail -> {
                        Detail(id = page.id)
                    }
                    is Page.Settings -> {
                        Settings()
                    }
                }
            }
        }
    }
}

@Composable
fun Search(onSearch: (query: String) -> Unit) {
    var query by rememberSaveable(key = "query") { mutableStateOf("") }
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = query,
        onValueChange = { query = it },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions { onSearch(query) }
    )
}

@Composable
fun SearchResults(text: String) {
    Box(Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center,
            text = text
        )
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun Home(onItemClick: (item: Int) -> Unit) {
    Column {
        for (i in 0 until 10) {
            ListItem(Modifier.clickable(onClick = {
                onItemClick(i)
            })) {
                Text("Item: $i")
            }
        }
    }
}

@Composable
fun Detail(id: Int) {
    Box(Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center,
            text = "Detail: $id"
        )
    }
}

@Composable
fun Settings() {
    Box(Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center,
            text = "Settings"
        )
    }
}
