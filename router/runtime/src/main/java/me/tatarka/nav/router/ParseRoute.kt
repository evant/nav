package me.tatarka.nav.router

import android.net.Uri
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

private val ROUTES_CACHE = ConcurrentHashMap<Class<*>, Class<*>>()

inline fun <reified T : Any> parseRoute(uri: Uri?): List<T> = parseRoute(uri, T::class)

@PublishedApi
@Suppress("UNCHECKED_CAST")
internal fun <T : Any> parseRoute(uri: Uri?, kclass: KClass<T>): List<T> {
    return ROUTES_CACHE.getOrPut(kclass.java) {
        Class.forName("${kclass.java.name}RoutesKt")
    }.getMethod("parseRoute", KClass::class.java, Uri::class.java)
        .invoke(null, kclass, uri ?: Uri.EMPTY) as List<T>
}