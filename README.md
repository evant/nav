# Nav

A simple declarative Android compose navigator

## Nav

### Usage

The core component is a `Navigator` that takes a list of pages. The top page will be displayed to
the user and pages in the backstack will have their state saved. You can use anything to represent
your page state, but I recommend using a sealed class. You should also use `saveInstanceState()` to
ensure the pages are remembered through process death.

```kotlin
sealed class Page : Parcelable {
   @Parcelize
   object List : Page()
   @Parcelize
   data class Detail(id: Int): Page()
}

var pages by savedInstanceState { listOf<Page>(Page.List) }

Navigator(pages = pages, onPopPage = { pages = pages.dropLast(1) }) {
   when (val page = pages.last()) {
       is List -> ListPage()
       is Detail -> DetailPage(id = page.id)
   }
}
```

This allows you to control the back stack however you want. Alternatively, if you want a more 
opinionated way to manipulate the backstack you can use the `BackStack` class. If will enforce a
starting destination and you can only push and pop the stack.

```kotlin
val backStack by savedInstanceState { backStackOf<Page>(Page.List) } 

Navigator(backStack) {
   when (val page = backStack.current) {
       is List -> ListPage()
       is Detail -> DetailPage(id = page.id)
   }
}

...

backStack.navigate(page = Page.Detail(id = 1), popUpTo = { it is Page.List }, singleTop = true)
backStack.pop()
```

## Router

### Usage

You can annotate sealed class variants with `@Route("path")` and it will generate a `parseRoute()`
method to parse a path into the correct variant.

```kotlin
sealed class Page : Parcelable {
   @Parcelize
   @Route("")
   object List : Page()
   @Parcelize
   @Route("/detail/{id}")
   data class Detail(id: Int): Page()
}
...

val backStack = backStackOf(parseRoute(deepLink))
```

A helper is also provided to route your deep link from your activity into your compose tree. It is
recommend you set `android:launchMode="singleTop"` in your manifest and override `onNewIntent()`.
This ensures your Activity isn't recreated when receiving an intent. 

```kotlin
class MainActivity : ComponentActivity() {
    
    private val deepLinkHandler = DeepLinkHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var backStack by savedInstanceState { backStackOf<Page>(Page.Home) }
            deepLinkHandler.OnDeepLink { link -> backStack = backStackOf(parseRoute(link)) }
            App(backStack)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        deepLinkHandler.onNewIntent(intent)
    }
}
```