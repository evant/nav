package me.tatarka.nav.router

import android.net.Uri
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

private val ROUTES_CACHE = ConcurrentHashMap<Class<*>, Class<*>>()

inline fun <reified T : Any> Uri.toRoute(): T? = toRoute(T::class)

@PublishedApi
@Suppress("UNCHECKED_CAST")
internal fun <T : Any> Uri.toRoute(kclass: KClass<T>): T? {
    return ROUTES_CACHE.getOrPut(kclass.java) {
        Class.forName("${kclass.java.name}RoutesKt")
    }.getMethod("parseRoute", KClass::class.java, Uri::class.java)
        .invoke(null, kclass, this) as T?
}