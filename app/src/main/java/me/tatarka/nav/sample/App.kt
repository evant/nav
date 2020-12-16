package me.tatarka.nav.sample

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import me.tatarka.nav.router.parseRoute

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
    // The overall back stack
//    val backStack = rememberBackStack(deepLink) { backStackOf<Page>(parseRoute(deepLink)) }
//    // Page-specific back stacks
//    val searchBackStack =
//        rememberBackStack(deepLink) { backStackOf<SearchPage>(parseRoute(deepLink)) }
//    val homeBackStack =
//        rememberBackStack(deepLink) { backStackOf<HomePage>(parseRoute(deepLink)) }
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
                        icon = { Icon(imageVector = vectorResource(item.icon)) },
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
        Navigator(backStack.primary) {
            Box(Modifier.padding(padding)) {
                when (backStack.primary.current) {
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
