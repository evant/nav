package me.tatarka.nav.sample

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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

class AppBackStack(
    val primary: BackStack<Page>,
    val search: BackStack<SearchPage>,
    val home: BackStack<HomePage>,
)

@Composable
fun App(backStack: AppBackStack) {
    val pageBackStacks = mapOf(Page.Search to backStack.search, Page.Home to backStack.home)

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = when (backStack.primary.current) {
                        Page.Search -> stringResource(R.string.search)
                        Page.Home -> stringResource(R.string.app_name)
                        Page.Settings -> stringResource(R.string.settings)
                    },
                    style = MaterialTheme.typography.h4
                )
            })
        },
        bottomBar = {
            BottomNavigation {
                for (item in BOTTOM_ITEMS) {
                    BottomNavigationItem(
                        icon = { Icon(painterResource(item.icon), contentDescription = null) },
                        label = { Text(stringResource(item.title)) },
                        selected = item.page == backStack.primary.current,
                        onClick = {
                            if (item.page == backStack.primary.current) {
                                pageBackStacks[item.page]?.popToRoot()
                            } else {
                                backStack.primary.navigate(
                                    page = item.page,
                                    popUpTo = { it is Page.Home },
                                    singleTop = true,
                                )
                            }
                        })
                }
            }
        }) { padding ->
        Navigator(backStack.primary) { page ->
            Box(Modifier.padding(padding)) {
                when (page) {
                    Page.Search -> {
                        Search(backStack.search)
                    }
                    Page.Home -> {
                        Home(backStack.home)
                    }
                    Page.Settings -> {
                        Settings()
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun Search(backStack: BackStack<SearchPage>) {
    Navigator(backStack) { page ->
        pageTransition.AnimatedVisibility(
            visible = { page == it },
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            when (page) {
                SearchPage.Main -> {
                    SearchMain(onSearch = { query ->
                        backStack.navigate(SearchPage.Results(query), singleTop = true)
                    })
                }
                is SearchPage.Results -> {
                    SearchResults(text = page.query)
                }
            }
        }
    }
}

@Composable
fun SearchMain(onSearch: (query: String) -> Unit) {
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Home(backStack: BackStack<HomePage>) {
    Navigator(backStack) { page ->
        when (page) {
            HomePage.List -> {
                pageTransition.AnimatedVisibility(
                    visible = { it == page },
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    HomeList(onItemClick = { item ->
                        backStack.navigate(HomePage.Detail(item))
                    })
                }
            }
            is HomePage.Detail -> {
                pageTransition.AnimatedVisibility(
                    visible = { it == page },
                    enter = slideInHorizontally(),
                    exit = slideOutHorizontally() + fadeOut(),
                ) {
                    HomeDetail(id = page.id)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun HomeList(onItemClick: (item: Int) -> Unit) {
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
fun HomeDetail(id: Int) {
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
