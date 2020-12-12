package me.tatarka.nav.sample

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import me.tatarka.nav.*
import me.tatarka.nav.router.toRoute

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

@Composable
fun App(deepLink: Uri? = null) {
    // The overall back stack
    val backStack = rememberBackStack(deepLink) { deepLink.toBackStack<Page>(Page.Home) }
    // Page-specific back stacks
    val searchBackStack =
        rememberBackStack(deepLink) { deepLink.toBackStack<SearchPage>(SearchPage.Main) }
    val homeBackStack =
        rememberBackStack(deepLink) { deepLink.toBackStack<HomePage>(HomePage.List) }
    val pageBackStacks = mapOf(Page.Search to searchBackStack, Page.Home to homeBackStack)

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = when (backStack.current) {
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
                        icon = { Icon(imageVector = vectorResource(item.icon)) },
                        label = { Text(stringResource(item.title)) },
                        selected = item.page == backStack.current,
                        onClick = {
                            if (item.page == backStack.current) {
                                pageBackStacks[item.page]?.popToRoot()
                            } else {
                                backStack.navigate(
                                    page = item.page,
                                    popUpTo = { it is Page.Home },
                                    singleTop = true,
                                )
                            }
                        })
                }
            }
        }) { padding ->
        Navigator(backStack) {
            Box(Modifier.padding(padding)) {
                when (backStack.current) {
                    Page.Search -> {
                        Search(searchBackStack)
                    }
                    Page.Home -> {
                        Home(homeBackStack)
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
fun Search(backStack: BackStack<SearchPage>) {
    Navigator(backStack) {
        when (val page = backStack.current) {
            SearchPage.Main -> {
                var query by savedInstanceState { "" }
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = query,
                    onValueChange = { query = it },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    onImeActionPerformed = { action, _ ->
                        if (action == ImeAction.Search) {
                            backStack.navigate(SearchPage.Results(query), singleTop = true)
                        }
                    })
            }
            is SearchPage.Results -> {
                Box(Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center,
                        text = page.query
                    )
                }
            }
        }
    }
}

@Composable
fun Home(backStack: BackStack<HomePage>) {
    Navigator(backStack) {
        when (val page = backStack.current) {
            HomePage.List -> {
                Column {
                    for (i in 0 until 10) {
                        ListItem(Modifier.clickable(onClick = {
                            backStack.navigate(HomePage.Detail(i))
                        })) {
                            Text("Item: $i")
                        }
                    }
                }
            }
            is HomePage.Detail -> {
                Box(Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center,
                        text = "Detail: ${page.id}"
                    )
                }
            }
        }
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
